/*
 * Sistema de Empréstimos de Equipamentos — Além das Barreiras.
 * JavaScript puro: a interface apenas envia e exibe dados da API; regras de negócio e permissões são responsabilidade do backend.
 *
 * Lucas (tech lead): integração das telas com as rotas relativas do Spring Boot e organização do código para revisão.
 * Cleber e Thiago (segurança): fluxo de login JWT e envio do token no cabeçalho Authorization.
 * Thiago (ViaCEP): o formulário envia CEP e número; o endereço deve ser preenchido pelo serviço ViaCEP no backend.
 * Lara (QA): feedback acessível, mensagens de erro da API e ações fáceis de testar.
 * Filipe (documentação): textos, rótulos e identidade visual "Além das Barreiras" consistentes com a apresentação.
 */

'use strict';

// Cleber e Thiago — guarda o token somente durante a sessão atual do navegador.
const TOKEN_KEY = 'alemDasBarreirasToken';
const ROLE_KEY = 'alemDasBarreirasRole';
const NAME_KEY = 'alemDasBarreirasName';

const TIPOS_EQUIPAMENTO = {
  NOTEBOOK: 'Notebook',
  MONITOR: 'Monitor',
  HEADSET: 'Headset',
  TECLADO: 'Teclado',
  MOUSE: 'Mouse',
  OUTRO: 'Outro'
};

let equipamentosAtuais = [];
let colaboradoresAtuais = [];
let emprestimosAtuais = [];
let mensagemTimer = null;
let viewAtual = 'dashboard';

const elemento = (id) => document.getElementById(id);

// Lara — transforma texto externo em texto seguro antes de inserir valores recebidos da API nas tabelas.
function escaparHtml(valor) {
  return String(valor ?? '').replace(/[&<>"']/g, (caractere) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[caractere]);
}

// Lara — mostra mensagens de sucesso ou erro em uma região aria-live para leitores de tela.
function mostrarMensagem(texto, tipo = 'success', destino = 'globalMessage') {
  const caixa = elemento(destino);
  if (!caixa) return;
  caixa.textContent = texto;
  caixa.className = `message is-visible ${tipo === 'error' ? 'is-error' : 'is-success'}${destino === 'globalMessage' ? ' global-message' : ''}`;
  if (mensagemTimer) window.clearTimeout(mensagemTimer);
  mensagemTimer = window.setTimeout(() => {
    caixa.textContent = '';
    caixa.className = destino === 'globalMessage' ? 'message global-message' : 'message';
  }, 6500);
}

// Cleber e Thiago — interpreta o perfil presente no JWT sem substituir a validação de autorização do servidor.
function lerDadosToken(token) {
  try {
    let parte = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    parte += '='.repeat((4 - (parte.length % 4)) % 4);
    const payload = JSON.parse(decodeURIComponent(Array.from(atob(parte), (letra) => `%${letra.charCodeAt(0).toString(16).padStart(2, '0')}`).join('')));
    const perfilBruto = payload.perfil || payload.role || payload.authorities || payload.authority || '';
    const perfilTexto = Array.isArray(perfilBruto) ? perfilBruto.join(' ') : String(perfilBruto);
    return {
      perfil: perfilTexto.toUpperCase().includes('ADMIN') ? 'ADMIN' : 'COLABORADOR',
      nome: payload.nome || payload.usuario || payload.sub || 'Usuário'
    };
  } catch (erro) {
    return { perfil: 'COLABORADOR', nome: 'Usuário' };
  }
}

// Cleber e Thiago — encerra a sessão quando a API responde 401 e devolve o usuário à tela de login.
function encerrarSessao(motivo = '') {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(ROLE_KEY);
  sessionStorage.removeItem(NAME_KEY);
  elemento('appScreen').classList.add('is-hidden');
  elemento('loginScreen').classList.remove('is-hidden');
  elemento('loginForm').reset();
  if (motivo) mostrarMensagem(motivo, 'error', 'loginMessage');
}

// Lucas — concentra as chamadas HTTP, inclui o JWT e apresenta a mensagem "message" enviada pela API.
async function chamarApi(caminho, opcoes = {}) {
  const token = sessionStorage.getItem(TOKEN_KEY);
  const cabecalhos = { ...(opcoes.headers || {}) };
  if (token) cabecalhos.Authorization = `Bearer ${token}`;
  if (opcoes.body && !cabecalhos['Content-Type']) cabecalhos['Content-Type'] = 'application/json';

  let resposta;
  try {
    resposta = await fetch(caminho, { ...opcoes, headers: cabecalhos });
  } catch (erro) {
    throw new Error('Não foi possível conectar à API. Verifique se o Spring Boot está em execução.');
  }

  if (resposta.status === 401) {
    encerrarSessao('Sua sessão expirou ou não é válida. Entre novamente.');
    throw new Error('Sessão encerrada.');
  }

  if (resposta.status === 204) return null;

  const tipoConteudo = resposta.headers.get('content-type') || '';
  let dados = null;
  if (tipoConteudo.includes('application/json')) {
    try { dados = await resposta.json(); } catch (erro) { dados = null; }
  } else {
    const texto = await resposta.text();
    dados = texto ? { message: texto } : null;
  }

  if (!resposta.ok) {
    const mensagem = dados && dados.message ? dados.message : `A API retornou o erro ${resposta.status}.`;
    throw new Error(mensagem);
  }
  return dados;
}

// Lucas — aceita a lista simples documentada no PDF e a estrutura paginada comum do Spring, se o backend usar esse formato.
function normalizarLista(dados) {
  if (Array.isArray(dados)) return dados;
  if (dados && Array.isArray(dados.content)) return dados.content;
  if (dados && Array.isArray(dados.itens)) return dados.itens;
  return [];
}

// Lara — formata a data recebida da API para leitura em português, sem calcular prazos no navegador.
function formatarData(data) {
  if (!data) return '—';
  const partes = String(data).slice(0, 10).split('-');
  if (partes.length !== 3) return escaparHtml(data);
  return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

// Filipe — traduz os nomes técnicos do enum em rótulos mais fáceis de entender na tela.
function nomeTipo(tipo) {
  return TIPOS_EQUIPAMENTO[tipo] || tipo || '—';
}

// Cleber e Thiago — realiza o login e inicia a sessão com o token JWT devolvido pela API.
async function fazerLogin(evento) {
  evento.preventDefault();
  const botao = elemento('loginForm').querySelector('button[type="submit"]');
  botao.disabled = true;
  try {
    const usuario = elemento('loginUsuario').value.trim();
    const senha = elemento('loginSenha').value;
    const resposta = await fetch('/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ usuario, senha })
    });
    const dados = await resposta.json().catch(() => ({}));
    if (!resposta.ok) throw new Error(dados.message || 'Não foi possível entrar. Confira usuário e senha.');
    if (!dados.token) throw new Error('A API não retornou o token JWT esperado. Confira o contrato do login.');

    const informacoes = lerDadosToken(dados.token);
    const perfilResposta = String(dados.perfil || dados.role || informacoes.perfil).toUpperCase();
    const perfil = perfilResposta.includes('ADMIN') ? 'ADMIN' : informacoes.perfil;
    sessionStorage.setItem(TOKEN_KEY, dados.token);
    sessionStorage.setItem(ROLE_KEY, perfil);
    sessionStorage.setItem(NAME_KEY, dados.nome || dados.usuario || informacoes.nome || usuario);
    elemento('loginMessage').textContent = '';
    elemento('loginMessage').className = 'message';
    abrirAplicacao();
  } catch (erro) {
    mostrarMensagem(erro.message, 'error', 'loginMessage');
  } finally {
    botao.disabled = false;
  }
}

// Lucas — troca a tela de login pelo painel após a autenticação.
function abrirAplicacao() {
  elemento('loginScreen').classList.add('is-hidden');
  elemento('appScreen').classList.remove('is-hidden');
  elemento('userName').textContent = sessionStorage.getItem(NAME_KEY) || 'Usuário';
  const perfil = sessionStorage.getItem(ROLE_KEY) || 'COLABORADOR';
  elemento('userRole').textContent = perfil === 'ADMIN' ? 'Administrador' : 'Colaborador';
  document.querySelectorAll('.admin-only').forEach((item) => {
    item.classList.toggle('is-hidden', perfil !== 'ADMIN');
  });
  elemento('todayLabel').textContent = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'long' }).format(new Date());
  mudarTela('dashboard');
  carregarPainel();
}

// Lucas — troca entre as áreas do sistema e mantém o item atual identificado na navegação.
function mudarTela(nome) {
  viewAtual = nome;
  document.querySelectorAll('.view-section').forEach((secao) => secao.classList.add('is-hidden'));
  const secaoAtual = elemento(`view-${nome}`);
  if (secaoAtual) secaoAtual.classList.remove('is-hidden');
  document.querySelectorAll('[data-view]').forEach((botao) => {
    const ativo = botao.dataset.view === nome;
    botao.classList.toggle('is-active', ativo);
    if (ativo) botao.setAttribute('aria-current', 'page');
    else botao.removeAttribute('aria-current');
  });
  const titulos = { dashboard: 'Visão geral', equipamentos: 'Equipamentos', colaboradores: 'Colaboradores', emprestimos: 'Empréstimos', atrasados: 'Empréstimos atrasados' };
  elemento('topbarTitle').textContent = titulos[nome] || 'Sistema';
  elemento('sidebar').classList.remove('is-open');
  elemento('mobileMenuButton').setAttribute('aria-expanded', 'false');
  if (nome === 'equipamentos') carregarEquipamentos();
  if (nome === 'colaboradores') carregarColaboradores();
  if (nome === 'emprestimos') carregarDadosEmprestimos();
  if (nome === 'atrasados') carregarAtrasados();
}

// Lucas — carrega os dados principais para atualizar os indicadores do painel.
async function carregarPainel() {
  try {
    const [listaEquipamentos, listaEmprestimos] = await Promise.all([
      chamarApi('/equipamentos'), chamarApi('/emprestimos')
    ]);
    equipamentosAtuais = normalizarLista(listaEquipamentos);
    emprestimosAtuais = normalizarLista(listaEmprestimos);
    elemento('statTotal').textContent = equipamentosAtuais.length;
    elemento('statAvailable').textContent = equipamentosAtuais.filter((item) => item.disponivel === true).length;
    elemento('statLoaned').textContent = equipamentosAtuais.filter((item) => item.disponivel === false).length;
    elemento('statOverdue').textContent = emprestimosAtuais.filter((item) => item.atrasado === true).length;
  } catch (erro) {
    if (erro.message !== 'Sessão encerrada.') mostrarMensagem(erro.message, 'error');
  }
}

// Lucas — solicita a lista de equipamentos à API, aplicando nela o filtro de tipo escolhido.
async function carregarEquipamentos() {
  const tipo = elemento('filterEquipmentType').value;
  const caminho = tipo ? `/equipamentos?tipo=${encodeURIComponent(tipo)}` : '/equipamentos';
  try {
    const dados = await chamarApi(caminho);
    equipamentosAtuais = normalizarLista(dados);
    renderizarEquipamentos(equipamentosAtuais);
  } catch (erro) {
    if (erro.message !== 'Sessão encerrada.') mostrarMensagem(erro.message, 'error');
  }
}

// Lara — apresenta disponibilidade e ações administrativas em uma tabela acessível.
function renderizarEquipamentos(lista) {
  const corpo = elemento('equipmentTableBody');
  elemento('equipmentCount').textContent = `${lista.length} ${lista.length === 1 ? 'item' : 'itens'}`;
  if (!lista.length) {
    corpo.innerHTML = '<tr><td colspan="5" class="table-empty">Nenhum equipamento encontrado.</td></tr>';
    return;
  }
  const admin = sessionStorage.getItem(ROLE_KEY) === 'ADMIN';
  corpo.innerHTML = lista.map((item) => {
    const disponivel = item.disponivel === true;
    const status = disponivel ? '<span class="status status-available">Disponível</span>' : '<span class="status status-loaned">Emprestado</span>';
    const acao = admin ? `<td><button class="button button-secondary button-small" type="button" data-editar-equipamento="${escaparHtml(item.id)}">Editar</button></td>` : '';
    return `<tr>
      <td><span class="table-main">${escaparHtml(item.patrimonio)}</span></td>
      <td><span class="table-main">${escaparHtml(item.nome)}</span></td>
      <td>${escaparHtml(nomeTipo(item.tipo))}</td>
      <td>${status}</td>
      ${acao}
    </tr>`;
  }).join('');
}

// Lucas — envia somente patrimônio, nome e tipo; a API define o equipamento como disponível.
async function cadastrarEquipamento(evento) {
  evento.preventDefault();
  const formulario = evento.currentTarget;
  const dados = Object.fromEntries(new FormData(formulario).entries());
  try {
    await chamarApi('/equipamentos', { method: 'POST', body: JSON.stringify(dados) });
    formulario.reset();
    mostrarMensagem('Equipamento cadastrado com sucesso.');
    await carregarEquipamentos();
    await carregarPainel();
  } catch (erro) {
    if (erro.message !== 'Sessão encerrada.') mostrarMensagem(erro.message, 'error');
  }
}

// Lucas — abre o formulário de edição para os campos permitidos pela rota PUT do projeto.
function abrirEdicaoEquipamento(id) {
  const equipamento = equipamentosAtuais.find((item) => String(item.id) === String(id));
  if (!equipamento) return;
  elemento('editEquipmentId').value = equipamento.id;
  elemento('editEquipmentName').value = equipamento.nome || '';
  elemento('editEquipmentType').value = equipamento.tipo || 'OUTRO';
  elemento('editEquipmentDialog').showModal();
  elemento('editEquipmentName').focus();
}

// Lucas — envia a atualização do nome e tipo à API, sem alterar o patrimônio ou disponibilidade.
async function atualizarEquipamento(evento) {
  evento.preventDefault();
  const id = elemento('editEquipmentId').value;
  const dados = { nome: elemento('editEquipmentName').value, tipo: elemento('editEquipmentType').value };
  try {
    await chamarApi(`/equipamentos/${encodeURIComponent(id)}`, { method: 'PUT', body: JSON.stringify(dados) });
    elemento('editEquipmentDialog').close();
    mostrarMensagem('Equipamento atualizado com sucesso.');
    await carregarEquipamentos();
    await carregarPainel();
  } catch (erro) {
    if (erro.message !== 'Sessão encerrada.') mostrarMensagem(erro.message, 'error');
  }
}

// Thiago — envia somente nome, e-mail, CEP e número; o endereço é obtido no backend via ViaCEP.
async function cadastrarColaborador(evento) {
  evento.preventDefault();
  const formulario = evento.currentTarget;
  const dados = Object.fromEntries(new FormData(formulario).entries());
  try {
    const resposta = await chamarApi('/colaboradores', { method: 'POST', body: JSON.stringify(dados) });
    formulario.reset();
    const endereco = resposta && resposta.logradouro
      ? ` Endereço retornado: ${[resposta.logradouro, resposta.bairro, resposta.cidade, resposta.uf].filter(Boolean).join(', ')}.`
      : ' O endereço será consultado pelo serviço ViaCEP da API.';
    mostrarMensagem(`Colaborador cadastrado com sucesso.${endereco}`);
    await carregarColaboradores();
  } catch (erro) {
    if (erro.message !== 'Sessão encerrada.') mostrarMensagem(erro.message, 'error');
  }
}

// Lucas — busca os colaboradores pela rota administrativa documentada.
async function carregarColaboradores() {
  try {
    const dados = await chamarApi('/colaboradores');
    colaboradoresAtuais = normalizarLista(dados);
    renderizarColaboradores(colaboradoresAtuais);
    preencherSelecaoColaboradores();
  } catch (erro) {
    if (erro.message !== 'Sessão encerrada.') mostrarMensagem(erro.message, 'error');
  }
}

// Lara — mostra os dados de endereço devolvidos pela API em uma tabela de leitura.
function renderizarColaboradores(lista) {
  const corpo = elemento('collaboratorTableBody');
  elemento('collaboratorCount').textContent = `${lista.length} ${lista.length === 1 ? 'pessoa' : 'pessoas'}`;
  if (!lista.length) {
    corpo.innerHTML = '<tr><td colspan="4" class="table-empty">Nenhum colaborador cadastrado.</td></tr>';
    return;
  }
  corpo.innerHTML = lista.map((item) => {
    const endereco = [item.logradouro, item.numero, item.bairro, item.cidade, item.uf].filter(Boolean).join(', ');
    return `<tr>
      <td><span class="table-main">${escaparHtml(item.nome)}</span></td>
      <td>${escaparHtml(item.email)}</td>
      <td>${escaparHtml(item.cep)}</td>
      <td>${escaparHtml(endereco || 'Endereço não informado')}</td>
    </tr>`;
  }).join('');
}

// Lucas — atualiza a seleção de colaboradores usando os registros retornados pelo backend.
function preencherSelecaoColaboradores() {
  const seletor = elemento('loanCollaborator');
  seletor.innerHTML = '<option value="">Selecione um colaborador</option>' + colaboradoresAtuais.map((item) =>
    `<option value="${escaparHtml(item.id)}">${escaparHtml(item.nome)} — ${escaparHtml(item.email)}</option>`
  ).join('');
}

// Lucas — pede à API somente equipamentos disponíveis para preencher a seleção do empréstimo.
async function carregarEquipamentosDisponiveis() {
  try {
    const dados = await chamarApi('/equipamentos?disponivel=true');
    const lista = normalizarLista(dados);
    elemento('loanEquipment').innerHTML = '<option value="">Selecione um equipamento</option>' + lista.map((item) =>
      `<option value="${escaparHtml(item.id)}">${escaparHtml(item.patrimonio)} — ${escaparHtml(item.nome)}</option>`
    ).join('');
  } catch (erro) {
    if (erro.message !== 'Sessão encerrada.') mostrarMensagem(erro.message, 'error');
  }
}

// Lucas — carrega empréstimos e dados necessários aos formulários administrativos.
async function carregarDadosEmprestimos() {
  try {
    const dados = await chamarApi('/emprestimos');
    emprestimosAtuais = normalizarLista(dados);
    renderizarEmprestimos(emprestimosAtuais);
    if (sessionStorage.getItem(ROLE_KEY) === 'ADMIN') {
      await Promise.all([carregarEquipamentosDisponiveis(), carregarColaboradores()]);
    }
  } catch (erro) {
    if (erro.message !== 'Sessão encerrada.') mostrarMensagem(erro.message, 'error');
  }
}

// Lara — renderiza o histórico e oferece devolução para empréstimos ainda sem data de devolução.
function renderizarEmprestimos(lista) {
  const corpo = elemento('loanTableBody');
  elemento('loanCount').textContent = `${lista.length} ${lista.length === 1 ? 'registro' : 'registros'}`;
  if (!lista.length) {
    corpo.innerHTML = '<tr><td colspan="6" class="table-empty">Nenhum empréstimo registrado.</td></tr>';
    return;
  }
  const admin = sessionStorage.getItem(ROLE_KEY) === 'ADMIN';
  corpo.innerHTML = lista.map((item) => {
    const equipamento = item.equipamento || {};
    const colaborador = item.colaborador || {};
    const devolvido = Boolean(item.dataDevolucao);
    const atrasado = item.atrasado === true;
    const status = devolvido
      ? '<span class="status status-returned">Devolvido</span>'
      : atrasado ? '<span class="status status-overdue">Atrasado</span>' : '<span class="status status-loaned">Em aberto</span>';
    const acao = admin && !devolvido
      ? `<td><button class="button button-secondary button-small" type="button" data-devolver-emprestimo="${escaparHtml(item.id)}">Registrar devolução</button></td>`
      : (admin ? '<td>—</td>' : '');
    return `<tr>
      <td><span class="table-main">${escaparHtml(equipamento.nome || 'Equipamento')}</span><span class="table-sub">${escaparHtml(equipamento.patrimonio || '')}</span></td>
      <td><span class="table-main">${escaparHtml(colaborador.nome || 'Colaborador')}</span></td>
      <td>${formatarData(item.dataEmprestimo)}</td>
      <td>${formatarData(item.dataPrevistaDevolucao)}</td>
      <td>${status}</td>
      ${acao}
    </tr>`;
  }).join('');
}

// Lucas — envia os IDs escolhidos; a API valida disponibilidade e define as datas do empréstimo.

async function registrarEmprestimo(evento) {
  evento.preventDefault();

  // Guarda o formulário antes do await
  const formulario = evento.currentTarget;

  const dadosFormulario = new FormData(formulario);

  const dados = {
    equipamentoId: Number(dadosFormulario.get('equipamentoId')),
    colaboradorId: Number(dadosFormulario.get('colaboradorId'))
  };

  try {
    await chamarApi('/emprestimos', {
      method: 'POST',
      body: JSON.stringify(dados)
    });

    // Usa a referência guardada, não evento.currentTarget
    formulario.reset();

    mostrarMensagem('Empréstimo registrado com sucesso.');

    await Promise.all([
      carregarDadosEmprestimos(),
      carregarPainel()
    ]);

  } catch (erro) {
    if (erro.message !== 'Sessão encerrada.') {
      mostrarMensagem(erro.message, 'error');
    }
  }
}


// Lucas — registra a devolução na rota indicada no documento, sem alterar o estado no navegador.
async function registrarDevolucao(id) {
  try {
    await chamarApi(`/emprestimos/${encodeURIComponent(id)}/devolucao`, { method: 'PUT' });
    mostrarMensagem('Devolução registrada com sucesso.');
    await Promise.all([carregarDadosEmprestimos(), carregarPainel()]);
  } catch (erro) {
    if (erro.message !== 'Sessão encerrada.') mostrarMensagem(erro.message, 'error');
  }
}

// Lara — consulta o filtro atrasados=true na API; a tela não calcula atraso por conta própria.
async function carregarAtrasados() {
  try {
    const dados = await chamarApi('/emprestimos?atrasados=true');
    const lista = normalizarLista(dados);
    renderizarAtrasados(lista);
  } catch (erro) {
    if (erro.message !== 'Sessão encerrada.') mostrarMensagem(erro.message, 'error');
  }
}

// Lara — exibe os empréstimos que o backend devolveu como atrasados.
function renderizarAtrasados(lista) {
  const corpo = elemento('overdueTableBody');
  elemento('overdueCount').textContent = `${lista.length} ${lista.length === 1 ? 'atrasado' : 'atrasados'}`;
  if (!lista.length) {
    corpo.innerHTML = '<tr><td colspan="5" class="table-empty">Não há empréstimos atrasados.</td></tr>';
    return;
  }
  corpo.innerHTML = lista.map((item) => {
    const equipamento = item.equipamento || {};
    const colaborador = item.colaborador || {};
    return `<tr>
      <td><span class="table-main">${escaparHtml(equipamento.patrimonio || '—')}</span></td>
      <td>${escaparHtml(equipamento.nome || '—')}</td>
      <td>${escaparHtml(colaborador.nome || '—')}</td>
      <td>${escaparHtml(colaborador.email || '—')}</td>
      <td><span class="status status-overdue">${formatarData(item.dataPrevistaDevolucao)}</span></td>
    </tr>`;
  }).join('');
}

// Lara — registra os eventos da interface em um único ponto para facilitar os testes e a manutenção.
function configurarEventos() {
  elemento('loginForm').addEventListener('submit', fazerLogin);
  elemento('logoutButton').addEventListener('click', () => encerrarSessao());
  elemento('equipmentForm').addEventListener('submit', cadastrarEquipamento);
  elemento('collaboratorForm').addEventListener('submit', cadastrarColaborador);
  elemento('loanForm').addEventListener('submit', registrarEmprestimo);
  elemento('editEquipmentForm').addEventListener('submit', atualizarEquipamento);
  elemento('closeEditDialog').addEventListener('click', () => elemento('editEquipmentDialog').close());
  elemento('cancelEditDialog').addEventListener('click', () => elemento('editEquipmentDialog').close());
  elemento('filterEquipmentType').addEventListener('change', carregarEquipamentos);
  elemento('refreshEquipamentos').addEventListener('click', carregarEquipamentos);
  elemento('refreshColaboradores').addEventListener('click', carregarColaboradores);
  elemento('refreshLoans').addEventListener('click', carregarDadosEmprestimos);
  elemento('refreshOverdue').addEventListener('click', carregarAtrasados);

  document.querySelectorAll('[data-view]').forEach((botao) => botao.addEventListener('click', () => mudarTela(botao.dataset.view)));
  document.querySelectorAll('[data-go-view]').forEach((botao) => botao.addEventListener('click', () => mudarTela(botao.dataset.goView)));

  elemento('equipmentTableBody').addEventListener('click', (evento) => {
    const botao = evento.target.closest('[data-editar-equipamento]');
    if (botao) abrirEdicaoEquipamento(botao.dataset.editarEquipamento);
  });
  elemento('loanTableBody').addEventListener('click', (evento) => {
    const botao = evento.target.closest('[data-devolver-emprestimo]');
    if (botao) registrarDevolucao(botao.dataset.devolverEmprestimo);
  });
  elemento('mobileMenuButton').addEventListener('click', () => {
    const aberto = elemento('sidebar').classList.toggle('is-open');
    elemento('mobileMenuButton').setAttribute('aria-expanded', String(aberto));
  });
}

// Lucas — inicia os eventos e restaura a sessão da aba quando a página é atualizada.
document.addEventListener('DOMContentLoaded', () => {
  configurarEventos();
  if (sessionStorage.getItem(TOKEN_KEY)) abrirAplicacao();
});

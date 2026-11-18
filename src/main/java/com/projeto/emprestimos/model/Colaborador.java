package com.projeto.emprestimos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "colaboradores")
public class Colaborador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "é obrigatório")
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank(message = "é obrigatório")
    @Email(message = "deve ser um e-mail válido")
    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @NotBlank(message = "é obrigatório")
    @Pattern(regexp = "\\d{8}", message = "deve conter exatamente 8 dígitos")
    @Column(nullable = false, length = 8)
    private String cep;

    @NotBlank(message = "é obrigatório")
    @Column(nullable = false, length = 20)
    private String numero;

    // Thiago: esses quatro campos vêm da API ViaCEP, não são informados manualmente na tela.
    private String logradouro;
    private String bairro;
    private String cidade;
    private String uf;

    public Colaborador() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
}

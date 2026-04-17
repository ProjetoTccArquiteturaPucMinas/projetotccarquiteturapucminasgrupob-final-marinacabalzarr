package com.seuprojeto.marketplace.application.usecase;

import com.seuprojeto.marketplace.application.dto.SelecaoCarrinho;
import com.seuprojeto.marketplace.domain.model.ResumoCarrinho;
import com.seuprojeto.marketplace.domain.repository.ProdutoRepositorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CalcularCarrinhoUseCase {

    private final ProdutoRepositorio produtoRepositorio;

    public CalcularCarrinhoUseCase(ProdutoRepositorio produtoRepositorio) {
        this.produtoRepositorio = produtoRepositorio;
    }

    public ResumoCarrinho executar(List<SelecaoCarrinho> selecaoCarrinhos) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal percentualDesconto = BigDecimal.ZERO;

        int quantidadeTotalItens = 0;

        for (SelecaoCarrinho selecaoCarrinho : selecaoCarrinhos) {
            var produto = produtoRepositorio.findById(selecaoCarrinho.getIdProduto())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + selecaoCarrinho.getIdProduto()));

            BigDecimal precoItem = produto.getPreco();
            BigDecimal quantidade = BigDecimal.valueOf(selecaoCarrinho.getQuantidade());
            subtotal = subtotal.add(precoItem.multiply(quantidade));

            quantidadeTotalItens += selecaoCarrinho.getQuantidade();

            percentualDesconto = percentualDesconto.add(
                    obterDescontoPorCategoria(produto.getCategoriaProduto().name())
            );
        }

        percentualDesconto = percentualDesconto.add(
                obterDescontoPorQuantidade(quantidadeTotalItens)
        );

        if (percentualDesconto.compareTo(new BigDecimal("25")) > 0) {
            percentualDesconto = new BigDecimal("25");
        }

        BigDecimal valorDesconto = subtotal
                .multiply(percentualDesconto)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        return new ResumoCarrinho(subtotal, valorDesconto);
    }

    private BigDecimal obterDescontoPorQuantidade(int quantidadeItens) {
        if (quantidadeItens == 2) {
            return new BigDecimal("5");
        }
        if (quantidadeItens == 3) {
            return new BigDecimal("7");
        }
        if (quantidadeItens >= 4) {
            return new BigDecimal("10");
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal obterDescontoPorCategoria(String categoria) {
        return switch (categoria) {
            case "CAPINHA" -> new BigDecimal("3");
            case "CARREGADOR" -> new BigDecimal("5");
            case "FONE" -> new BigDecimal("3");
            case "PELICULA" -> new BigDecimal("2");
            case "SUPORTE" -> new BigDecimal("2");
            default -> BigDecimal.ZERO;
        };
    }
}
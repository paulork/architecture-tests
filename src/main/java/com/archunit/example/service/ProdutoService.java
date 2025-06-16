package com.archunit.example.service;

import com.archunit.example.model.domain.Produto;
import com.archunit.example.model.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProdutoService {

    private final ProdutoRepository prodRepository;

    public ProdutoService(ProdutoRepository prodRepository) {
        this.prodRepository = prodRepository;
    }

    public Produto save(Produto produto) {
        // String id = produto.getId(); // Unused variable removed
        return prodRepository.save(produto);
    }

    public Produto findById(String produto_id) {
        return prodRepository.findById(produto_id)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao foi encontrado."));
    }

    public void removeById(String produto_id) {
        prodRepository.deleteById(produto_id);
    }

}

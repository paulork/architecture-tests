package com.archunit.example.controller;

import com.archunit.example.model.domain.Produto;
import com.archunit.example.model.repository.ProdutoRepository;
import com.archunit.example.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/produto")
public class ProdutoController {

    @Autowired
    private ProdutoService prodService;

    @Autowired
    private ProdutoRepository prodRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Produto save(@RequestBody Produto produto) {
        return prodService.save(produto);
    }

    @GetMapping("/{produto_id}")
    @ResponseStatus(HttpStatus.OK)
    public Produto findById(@PathVariable String produto_id) {
        return prodService.findById(produto_id);
    }

    @DeleteMapping("/{produto_id}")
    @ResponseStatus(HttpStatus.OK)
    public void removeById(@PathVariable String produto_id) {
        prodService.removeById(produto_id);
    }

}

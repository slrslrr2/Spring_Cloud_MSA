package com.gbitkim.catalogservice.controller;

import com.gbitkim.catalogservice.jpa.CatalogEntity;
import com.gbitkim.catalogservice.service.CatalogService;
import com.gbitkim.catalogservice.vo.ResponseCatalog;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService catalogService;

    @GetMapping("/health_check")
    public String status(){
        return "It's Working in User Service";
    }

    @GetMapping("/catalogs")
    public ResponseEntity<?> getCatalogs(){
        Iterable<CatalogEntity> allCatalogs = catalogService.getAllCatalogs();

        ArrayList<ResponseCatalog> responseCatalog = new ArrayList<>();
        allCatalogs.forEach(u -> responseCatalog.add(new ModelMapper().map(u, ResponseCatalog.class)));

        return new ResponseEntity<>(responseCatalog, HttpStatus.OK);
    }
}

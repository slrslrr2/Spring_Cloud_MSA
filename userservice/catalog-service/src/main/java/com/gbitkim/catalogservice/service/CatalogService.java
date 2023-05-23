package com.gbitkim.catalogservice.service;

import com.gbitkim.catalogservice.jpa.CatalogEntity;

import java.util.List;

public interface CatalogService {
    Iterable<CatalogEntity> getAllCatalogs();
}

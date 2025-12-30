package com.example.banhkem.service;

import com.example.banhkem.entity.Cake;
import com.example.banhkem.repository.CakeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CakeService {
    @Autowired private CakeRepository cakeRepository;

    public List<Cake> getAllCakes() { return cakeRepository.findAll(); }
    public Cake getCakeById(Long id) { return cakeRepository.findById(id).orElse(null); }
    public List<Cake> getCakesByCategory(Long catId) { return cakeRepository.findByCategoryId(catId); }
    public List<Cake> searchCakes(String name) { return cakeRepository.findByNameContainingIgnoreCase(name); }
    public Cake saveCake(Cake cake) { return cakeRepository.save(cake); }
    public void deleteCake(Long id) { cakeRepository.deleteById(id); }
}
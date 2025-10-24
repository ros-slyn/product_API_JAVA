package com.setec.controller;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.setec.dao.PostProductDAO;
import com.setec.dao.PutProductDAO;
import com.setec.entities.Product;
import com.setec.repos.ProductRepo;


//http://localhost:8080/swagger-ui/index.html

@RestController
@RequestMapping("/api/product")
public class MyController {
	
	@Autowired
	private ProductRepo productRepo;
	
	@GetMapping
	public Object getAll() {

		var product = productRepo.findAll();
		
		if(product.size()==0) {
			return ResponseEntity.status(404).body(Map.of("message", "Product is empty"));
		}
		
		return product;
	}
	
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Object postProduct(@ModelAttribute PostProductDAO product) throws Exception {
		String uploadDir = new File("myApp/static").getAbsolutePath();
		File dir =  new File(uploadDir);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		var file = product.getFile();
		
		String extensiion = Objects.requireNonNull(file.getOriginalFilename());
		String fileName = UUID.randomUUID()+"-"+extensiion;
		
		String filePath = Paths.get(uploadDir, fileName).toString();
		
		file.transferTo(new File(filePath));
		
		Product pro = new Product();
		pro.setName(product.getName());
		pro.setPrice(product.getPrice());
		pro.setQty(product.getQty());
		pro.setImageUrl("/static/"+fileName);
		
		productRepo.save(pro);
		
		
		return ResponseEntity.status(201).body(pro);
	}
	
	
	@GetMapping({"{id}", "id/{id}"})
	  public Object getById(@PathVariable("id") Integer id) {
	    var pro = productRepo.findById(id);
	    if(pro.isPresent()) {
	      return pro.get();
	    }
	    return ResponseEntity.status(404)
	       .body (Map.of("message","Product id = "+id+" not found"));
	  }
	
	
	@GetMapping("name/{name}")
	public Object getByName(@PathVariable("name") String name) {
		List<Product> pro = productRepo.findByName(name);
		if(pro.size()>0) {
			return pro;
		}
		return ResponseEntity.status(400).body(Map.of("message", "Product name: "+ name +" not fount"));
	}
		
		
	@DeleteMapping({"{id}","id/{id}"})
	public Object deleteById(@PathVariable("id") Integer id) {
		
		var p = productRepo.findById(id);
		if(p.isPresent()) {
			new File("myApp/"+p.get().getImageUrl()).delete();
			productRepo.delete(p.get());
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("message","Product id = "+id+" has been DELETED..."));
		}
		
		return ResponseEntity.status(404).body(Map.of("message","Product id = "+id+ " NOT FOUND"));		
	}
	
	
	@PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Object putProduct(@ModelAttribute PutProductDAO product) throws Exception {
		Integer id = product.getId();
		var p = productRepo.findById(id);
		if(p.isPresent()) {
			var update = p.get();
			update.setName(product.getName());
			update.setPrice(product.getPrice());
			update.setQty(product.getQty());
			
			if(product.getFile() != null) {
				String uploadDir = new File("myApp/static").getAbsolutePath();
				File dir =  new File(uploadDir);
				if(!dir.exists()) {
					dir.mkdirs();
				}
				var file = product.getFile();
				
				String extensiion = Objects.requireNonNull(file.getOriginalFilename());
				String fileName = UUID.randomUUID()+"-"+extensiion;				
				String filePath = Paths.get(uploadDir, fileName).toString();			
				
				new File("myApp/"+update.getImageUrl()).delete();
				
				file.transferTo(new File(filePath));
				update.setImageUrl("/static/"+fileName);
			}
			
			productRepo.save(update);
			return ResponseEntity.status(HttpStatus.ACCEPTED)
					 .body(Map.of("message", "Product id =" +id+ "update successful",
				              "product",update
				              ));
		}
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","Product id = "+id+ " NOT FOUND"));	
	}
	
	
}

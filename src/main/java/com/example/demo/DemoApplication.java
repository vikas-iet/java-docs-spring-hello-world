package com.example.demo;

import com.example.demo.dto.ProductDTO;
import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@SpringBootApplication
@Controller
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	public static String uploadDir= System.getProperty("user.dir")+"/src/main/resources/static/productImages";
	@Autowired
	CategoryService categoryService;
	@Autowired
	ProductService productService;
	@RequestMapping("/")
	String sayHello() {
		return "Hello World!";
	}

	@GetMapping("/admin")
	public String adminHome(){
		return "adminHome";
	}


	@GetMapping("/admin/categories")
	public String getCat(Model model){
		model.addAttribute("categories", categoryService.getAllCategory());
		return "categories";
	}

	@GetMapping ("/admin/categories/add")
	public String getCatAdd(Model model){
		model.addAttribute("category", new Category());
		return "categoriesAdd";
	}

	@PostMapping("/admin/categories/add")
	public String postCatAdd(@ModelAttribute("category") Category category){
		categoryService.addCategory(category);
		return "redirect:/admin/categories";
	}

	@GetMapping("/admin/categories/delete/{id}")
	public String deleteCategory(@PathVariable int id){
		categoryService.removeCategoryById(id);
		return "redirect:/admin/categories";
	}

	@GetMapping("/admin/categories/update/{id}")
	public String updateCategory(@PathVariable int id, Model model){
		Optional<Category> category = categoryService.getCategoryById(id);
		if(category.isPresent()){
			model.addAttribute("category", category.get() );
			return "categoriesAdd";
		}else {
			return "404";
		}
	}

	//Product Section........

	@GetMapping("/admin/products")
	public String products(Model model, @RequestParam(defaultValue = "") String searchKey){
		model.addAttribute("products", productService.getAllProduct(searchKey));
		return "products";
	}

	@GetMapping("/admin/products/add")
	public String addProductGet(Model model){
		model.addAttribute("productDTO", new ProductDTO());
		model.addAttribute("categories", categoryService.getAllCategory());
		return "productsAdd";
	}

	@PostMapping("/admin/products/add")
	public String postProductAdd(@ModelAttribute("productDTO") ProductDTO productDTO,
								 @RequestParam("productImage") MultipartFile file,
								 @RequestParam("imgName") String imgName) throws IOException {
		Product product= new Product();
		product.setId(productDTO.getId());
		product.setName(productDTO.getName());
		product.setCategory(categoryService.getCategoryById(productDTO.getCategoryId()).get());
		product.setPrice(productDTO.getPrice());
		product.setWeight(productDTO.getWeight());
		product.setDescription(productDTO.getDescription());
		product.setImageName(productDTO.getImageName());
		String imageUUID;
		if(!file.isEmpty()){
			imageUUID = file.getOriginalFilename();
			Path fileNameAndPath = Paths.get(uploadDir, imageUUID);
			Files.write(fileNameAndPath, file.getBytes());
		}else{
			imageUUID= imgName;
		}
		product.setImageName(imageUUID);
		productService.addProduct(product);
		return "redirect:/admin/products";
	}

	@GetMapping("/admin/product/delete/{id}")
	public String deleteProduct(@PathVariable Long id){
		productService.removeProductById(id);
		return "redirect:/admin/products";
	}

	@GetMapping("/admin/product/update/{id}")
	public String updateProducts(@PathVariable Long id, Model model){
		Product product = productService.getProductById(id).get();
		ProductDTO productDTO= new ProductDTO();
		productDTO.setName(product.getName());
		productDTO.setPrice(product.getPrice());
		productDTO.setId(product.getId());
		productDTO.setWeight(product.getWeight());
		productDTO.setDescription(product.getDescription());
		productDTO.setCategoryId(product.getCategory().getId());
		productDTO.setImageName(product.getImageName());
		model.addAttribute("categories",categoryService.getAllCategory());
		model.addAttribute("productDTO", productDTO);
		return "productsAdd";
	}
	@GetMapping({"/","/home"})
	public String home(Model model){
		return "index";
	}

	@GetMapping("/shop")
	public String shop(Model model, @RequestParam(defaultValue = "") String searchKey){
		model.addAttribute("categories", categoryService.getAllCategory());
		model.addAttribute("products", productService.getAllProduct(searchKey));
		return "shop";
	}
	@GetMapping("/shop/category/{id}")
	public String shopByCategory(Model model, @PathVariable int id){
		model.addAttribute("categories", categoryService.getAllCategory());
		model.addAttribute("products", productService.getAllProductsByCategoryId(id));
		return "shop";
	}

	@GetMapping("/shop/viewproduct/{id}")
	public String viewProduct(Model model, @PathVariable Long id){
		model.addAttribute("product", productService.getProductById(id).get());
		return "viewProduct";
	}
}

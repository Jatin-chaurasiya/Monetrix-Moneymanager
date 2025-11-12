package in.chaurasiya.moneymanager.Controller;


import in.chaurasiya.moneymanager.Service.CategoryService;
import in.chaurasiya.moneymanager.dto.CategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDTO> saveCategory(@RequestBody CategoryDTO categoryDTO) {
        CategoryDTO savedCategory = categoryService.saveCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        List<CategoryDTO> categories = categoryService.getCategoriesForCurrentUser();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{type}")
   public ResponseEntity<List<CategoryDTO>> getCategoriesTypeForCurrentUser(@PathVariable String type){
   List<CategoryDTO> list = categoryService.getCategoriesTypeForCurrentUser(type);
   return ResponseEntity.ok(list);
   }

   @PutMapping("/{categoryId}")
   public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long categoryId,
                                                     @RequestBody CategoryDTO categoryDTO){
        CategoryDTO updateCategory = categoryService.updateCategory(categoryId,categoryDTO);
       return ResponseEntity.ok(updateCategory);
   }
}

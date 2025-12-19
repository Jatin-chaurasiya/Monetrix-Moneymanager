package in.chaurasiya.moneymanager.Service;

import in.chaurasiya.moneymanager.Entity.CategoryEntity;
import in.chaurasiya.moneymanager.Entity.ProfileEntity;
import in.chaurasiya.moneymanager.Repository.CategoryRepository;
import in.chaurasiya.moneymanager.dto.CategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;

    //save category here Use toEntity helper method
    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        if (categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), profile.getId())) {
            throw new RuntimeException("Category with this name already exists");
        }

        CategoryEntity newCategory = toEntity(categoryDTO, profile);
        newCategory = categoryRepository.save(newCategory);
        return toDTO(newCategory);
    }

    //get categories for current user
    public List<CategoryDTO> getCategoriesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepository.findByProfileId(profile.getId());
        return categories.stream().map(this::toDTO).toList();
    }
    //Delete category
    public void deleteCategory(Long id){

    }


    public List<CategoryDTO> getCategoriesTypeForCurrentUser(String type){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> enities = categoryRepository.findByTypeAndProfileId(type,profile.getId());
        return enities.stream().map(this::toDTO).toList();
    }

    public CategoryDTO updateCategory(Long categoryId,CategoryDTO dto){
        ProfileEntity profile = profileService.getCurrentProfile();
         CategoryEntity categoryEntity =
                 categoryRepository.findByIdAndProfileId(categoryId,profile.getId()).orElseThrow(()-> new RuntimeException("Category " +
                "not " +
                "found or not accessible"));
         categoryEntity.setName(dto.getName());
         categoryEntity.setIcon(dto.getIcon());
         categoryEntity = categoryRepository.save(categoryEntity);
         return toDTO(categoryEntity);
    }

    //helper methods
    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile) {
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .profile(profile)
                .type(categoryDTO.getType())
                .build();
    }

    private CategoryDTO toDTO(CategoryEntity entity) {
        return CategoryDTO.builder()
                .id(entity.getId())
                .profileId(entity.getProfile() != null ?  entity.getProfile().getId(): null)
                .name(entity.getName())
                .icon(entity.getIcon())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .type(entity.getType())
                .build();

    }

}

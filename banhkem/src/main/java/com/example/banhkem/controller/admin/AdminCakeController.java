package com.example.banhkem.controller.admin;

import com.example.banhkem.entity.Cake;
import com.example.banhkem.entity.Category;
import com.example.banhkem.service.CakeService;
import com.example.banhkem.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Controller
@RequestMapping("/admin/cakes")
public class AdminCakeController {

    @Autowired private CakeService cakeService;
    @Autowired private CategoryService categoryService;

    // Đường dẫn gốc tới thư mục static
    private final String UPLOAD_DIR = "src/main/resources/static";

    @GetMapping
    public String list(Model model) {
        model.addAttribute("cakes", cakeService.getAllCakes());
        model.addAttribute("categories", categoryService.getAllCategories());
        Cake cake = new Cake();
        cake.setCategory(new Category());
        model.addAttribute("cake", cake);
        return "admin/cake";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Cake cake,
                       @RequestParam("imageFile") MultipartFile imageFile,
                       RedirectAttributes ra) throws IOException {

        Cake existingCake = null;
        if (cake.getId() != null) {
            existingCake = cakeService.getCakeById(cake.getId());
        }

        if (!imageFile.isEmpty()) {
            // 1. Nếu đang UPDATE và có ảnh cũ, hãy xóa ảnh cũ trước khi lưu ảnh mới
            if (existingCake != null && existingCake.getImage() != null) {
                deletePhysicalFile(existingCake.getImage());
            }

            // 2. Lưu ảnh mới
            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + "/images/" + fileName);
            Files.createDirectories(path.getParent());
            Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            cake.setImage("/images/" + fileName);
        } else {
            if (existingCake != null) {
                cake.setImage(existingCake.getImage());
            }
        }

        if (cake.getCategory() == null || cake.getCategory().getId() == null) {
            ra.addFlashAttribute("error", "Vui lòng chọn danh mục!");
            return "redirect:/admin/cakes";
        }

        cakeService.saveCake(cake);
        ra.addFlashAttribute("message", "Thao tác thành công!");
        return "redirect:/admin/cakes";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        Cake cake = cakeService.getCakeById(id);
        if (cake != null) {
            // 1. Xóa file ảnh vật lý trước khi xóa bản ghi trong DB
            if (cake.getImage() != null && !cake.getImage().isEmpty()) {
                deletePhysicalFile(cake.getImage());
            }
            // 2. Xóa trong Database
            cakeService.deleteCake(id);
            ra.addFlashAttribute("message", "Đã xóa sản phẩm và ảnh liên quan!");
        }
        return "redirect:/admin/cakes";
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public Cake getCakeToEdit(@PathVariable Long id) {
        Cake cake = cakeService.getCakeById(id);
        if (cake != null && cake.getCategory() != null) {
            cake.getCategory().setCakes(null);
        }
        return cake;
    }

    /**
     * Hàm dùng chung để xóa file vật lý dựa trên đường dẫn lưu trong DB
     */
    private void deletePhysicalFile(String imagePath) {
        try {
            // imagePath có dạng "/images/ten_file.jpg"
            // Cần chuyển thành "src/main/resources/static/images/ten_file.jpg"
            Path path = Paths.get(UPLOAD_DIR + imagePath);
            Files.deleteIfExists(path);
            System.out.println("Đã xóa file: " + path.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Không thể xóa file ảnh: " + e.getMessage());
        }
    }
}
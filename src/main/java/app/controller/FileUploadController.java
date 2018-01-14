package app.controller;

import app.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import app.StorageFileNotFoundException;

import java.io.IOException;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class FileUploadController {
    private  final Storage storage;

    //@Autowire используется для автоматического внедрения зависимости, Spring  находит экземпляр нужного бина и подставляет
//в свойство отмеченное аннотацией
 @Autowired
 public FileUploadController (Storage storage) {
        this.storage = storage;

    }
    //@GetMapping позволяет м связать HTTP запросы с определенными методами контроллера, в данном случае
    // метод связан с GET @GetMapping-
    @GetMapping
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storage.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody // сериализует объект into JSON  и передает назад в HttpResponse object

    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storage.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        storage.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
        public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}

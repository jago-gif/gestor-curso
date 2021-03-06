package cl.aiep.certif.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import cl.aiep.certif.controller.service.CursoService;
import cl.aiep.certif.controller.service.EstudianteService;
import cl.aiep.certif.dao.dto.ContenidoDTO;
import cl.aiep.certif.dao.dto.CursoDTO;

@Controller
public class CursosController {
	
	@Autowired
	CursoService service;
	
	@Autowired
	EstudianteService serviceEst;
	
	@GetMapping("/admin/crearCurso")
	@PreAuthorize("isAuthenticated()")
	public String crearCurso(final Model model){
		
		
		
		model.addAttribute("curso", new CursoDTO());
		
		return "nuevoCurso";
	}
	
	@GetMapping("/admin/editarCurso/{id}")
	@PreAuthorize("isAuthenticated()")
	public String editarCurso(final Model model , @PathVariable Integer id) {
		
		CursoDTO curso= service.obtenerCurso(id);
		model.addAttribute("curso", curso);
		
		return "nuevoCurso";
	}
	
	@GetMapping("/admin/eliminarCurso/{id}")
	@PreAuthorize("isAuthenticated()")
	public String eliminarCurso(@PathVariable Integer id) {
		service.eliminarCurso(id);
		 return "redirect:/admin/";
	}
	
	@GetMapping("/admin/agregarContenido/{id}")
	@PreAuthorize("isAuthenticated()")
	public String crearContenido(@PathVariable Integer id, final Model model) {
		ContenidoDTO contenidoDto= new ContenidoDTO();
		contenidoDto.setIdCurso(id);
		model.addAttribute("contenido", contenidoDto);
		model.addAttribute("contenidos", service.obtenerContenidos(id));
		
		
		return "contenido";
	}
	
	@PostMapping("/admin/guardarContenido")
	@PreAuthorize("isAuthenticated()")
	public String guardarContenido(@Valid ContenidoDTO contenido, BindingResult result, final Model model) {
		if (result.hasErrors()) {
			model.addAttribute("contenido", contenido);
			 model.addAttribute("mensaje", result.getFieldError().getDefaultMessage());
	        return "contenido";
		}
		service.guardarContenido(contenido);

        return "redirect:/admin/agregarContenido/"+contenido.getIdCurso();
	}
		
	@GetMapping("/admin/elimina/{id}")
	@PreAuthorize("isAuthenticated()")
	public String eliminarContenido(final Model model, @PathVariable Integer id) {
		
		service.eliminarContenido(id);

        return "redirect:/admin/";
	}
	
	
	
	@PostMapping("/admin/guardarCurso")
	@PreAuthorize("isAuthenticated()")
	public String guardarCurso(@Valid CursoDTO curso, BindingResult result, final Model model,@RequestParam(value="file")MultipartFile imagen) {
		if (result.hasErrors()) {
			model.addAttribute("curso", curso);
			 model.addAttribute("mensaje", result.getFieldError().getDefaultMessage());
	        return "nuevoCurso";
		}
		if(!imagen.isEmpty()){
			Path directorioImagenes= Paths.get("src//main//resources//static/images");
			String rutaAbsoluta = directorioImagenes.toFile().getAbsolutePath();
			try {
				byte[] byteImg = imagen.getBytes();
				Path rutaCompleta = Paths.get(rutaAbsoluta+"//"+imagen.getOriginalFilename());
				Files.write(rutaCompleta, byteImg);
				curso.setImagen("/images/"+imagen.getOriginalFilename());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(curso.getId()!=null) {
			service.actualizarCurso(curso);}
		else service.guardarCurso(curso);

        return "redirect:/admin";
	}
	

	
	@GetMapping("/obtiene/{id}")
	@PreAuthorize("isAuthenticated()")
	public String verCurso(final Model model, @PathVariable Integer id) {
		
		model.addAttribute("curso", service.obtenerCurso(id));
		model.addAttribute("contenidos", service.obtenerContenidos(id));

	return "curso";
	}

	
	@GetMapping("/asignarCurso/{id}")
	@PreAuthorize("isAuthenticated()")
	public String agregaCurso(@PathVariable Integer id, final Model model){
		 Authentication auth= SecurityContextHolder.getContext().getAuthentication();
		    if(service.obtieneCupos(id))
		      serviceEst.asignarCurso(auth.getName(), id);
		    else {
		    model.addAttribute("mensaje", "No tiene Cupos disponibles");
		    model.addAttribute("curso", service.obtenerCurso(id));
		    return "curso";
		    }
		    
		return "redirect:/";
	}
}

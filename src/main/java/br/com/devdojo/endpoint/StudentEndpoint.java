package br.com.devdojo.endpoint;

import br.com.devdojo.error.ResourceNotFoundException;
import br.com.devdojo.model.Student;
import br.com.devdojo.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;


@RestController
@RequestMapping("students")
public class StudentEndpoint {

    private final StudentRepository studentDAO;
    @Autowired
    public StudentEndpoint(StudentRepository studentDAO) {
        this.studentDAO = studentDAO;
    }

    @GetMapping
    public ResponseEntity <?> listAll(Pageable pageable){
        return new ResponseEntity<> (studentDAO.findAll(pageable), HttpStatus.OK);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity <?> getStudentById(@PathVariable("id") Long id,
                                             @AuthenticationPrincipal UserDetails userDetails){
        System.out.println(userDetails);
        Student student = studentDAO.findById(id).get();
        verifyIfStudentExists(id);
        if(!studentDAO.findById(id).isPresent())
           throw new ResourceNotFoundException("Student not found for ID: " +id);
        return new ResponseEntity<>(studentDAO, HttpStatus.OK);
    }

    @GetMapping (path = "/findByName/{name}")
    public ResponseEntity<?> findStudentsByName(@PathVariable String name ){

        return new ResponseEntity<>(studentDAO.findByNameIgnoreCaseContaining(name),HttpStatus.OK);
    }

    @PostMapping
    @Transactional(rollbackOn = Exception.class)
    public ResponseEntity <?> save(@Valid @RequestBody Student student){
        return new ResponseEntity<>(studentDAO.save(student),HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity <?> delete(@PathVariable Long id) {
        verifyIfStudentExists(id);
        studentDAO.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PutMapping
    public ResponseEntity <?> update(@RequestBody Student student) {
        verifyIfStudentExists(student.getId());
        studentDAO.save(student);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void verifyIfStudentExists(Long id){
        if (studentDAO.findById(id) == null);
        throw new ResourceNotFoundException("Student not found for ID"+id);
    }
}
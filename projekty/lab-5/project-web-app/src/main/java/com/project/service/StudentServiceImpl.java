package com.project.service;

import java.net.URI;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import com.project.exception.HttpException;
import com.project.model.Student;

@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger Logger = LoggerFactory.getLogger(StudentServiceImpl.class);
    private final RestClient restClient;

    public StudentServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    private String getResourcePath() {
        return "/api/studenti";
    }

    private String getResourcePath(Integer id) {
        return String.format("%s/%d", getResourcePath(), id);
    }

    @Override
    public Optional<Student> getStudent(Integer studentId) {
        String resourcePath = getResourcePath(studentId);
        Logger.info("REQUEST -> GET {}", resourcePath);
        Student student = restClient
                .get()
                .uri(resourcePath)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .body(Student.class);
        return Optional.ofNullable(student);
    }

    @Override
    public Student setStudent(Student student) {
        if (student.getStudentId() != null) {
            String resourcePath = getResourcePath(student.getStudentId());
            Logger.info("REQUEST -> PUT {}", resourcePath);
            restClient
                    .put()
                    .uri(resourcePath)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(student)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new HttpException(res.getStatusCode(), res.getHeaders());
                    })
                    .toBodilessEntity();
            return student;
        } else {
            String resourcePath = getResourcePath();
            Logger.info("REQUEST -> POST {}", resourcePath);
            ResponseEntity<Void> response = restClient
                    .post()
                    .uri(resourcePath)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(student)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new HttpException(res.getStatusCode(), res.getHeaders());
                    })
                    .toBodilessEntity();
            
            URI location = response.getHeaders().getLocation();
            return restClient
                    .get()
                    .uri(location)
                    .retrieve()
                    .body(Student.class);
        }
    }

    @Override
    public void deleteStudent(Integer studentId) {
        String resourcePath = getResourcePath(studentId);
        Logger.info("REQUEST -> DELETE {}", resourcePath);
        restClient
                .delete()
                .uri(resourcePath)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .toBodilessEntity();
    }

    @Override
    public Page<Student> getStudenci(Pageable pageable) {
        URI uri = ServiceUtil.getURI(getResourcePath(), pageable);
        Logger.info("REQUEST -> GET {}", uri);
        return restClient.get()
                .uri(uri.toString())
                .retrieve()
                .body(new ParameterizedTypeReference<RestResponsePage<Student>>() {});
    }

    @Override
    public Optional<Student> findByNrIndeksu(String nrIndeksu) {
        URI uri = UriComponentsBuilder.fromUriString(getResourcePath())
                .queryParam("nrIndeksu", nrIndeksu)
                .build().toUri();
        Logger.info("REQUEST -> GET {}", uri);
        Student student = restClient.get()
                .uri(uri)
                .retrieve()
                .body(Student.class);
        return Optional.ofNullable(student);
    }

    @Override
    public Page<Student> findByNazwiskoStartsWithIgnoreCase(String nazwisko, Pageable pageable) {
        URI uri = ServiceUtil.getUriComponent(getResourcePath(), pageable)
                .queryParam("nazwisko", nazwisko)
                .build().toUri();
        Logger.info("REQUEST -> GET {}", uri);
        return restClient.get()
                .uri(uri.toString())
                .retrieve()
                .body(new ParameterizedTypeReference<RestResponsePage<Student>>() {});
    }
}
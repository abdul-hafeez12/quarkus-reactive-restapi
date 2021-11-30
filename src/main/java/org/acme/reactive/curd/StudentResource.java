package org.acme.reactive.curd;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;
import javax.ws.rs.core.Response.Status;

@Path("/students")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StudentResource {

    @GET
    public Uni<List<Student>> getAllStudents() {
        return Student.listAll(Sort.by("name"));
    }

    @GET
    @Path(("/{id}"))
    public Uni<Student> getStudentById(@PathParam("id") Long id){
        return Student.findById(id);
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(@PathParam("id") Long id, Student updatedStudent) {
        if (updatedStudent == null)
            throw new WebApplicationException("Details were not updated on request", 422);
        return Panache.withTransaction( () -> Student.<Student>findById(id).
                        onItem().
                        ifNotNull().
                        invoke(
                                (student) -> {
                                    student.setName(updatedStudent.getName());
                                    student.setDepartment(updatedStudent.getDepartment());
                                    student.setSemester(updatedStudent.getSemester());
                                }
                        )
                )
                .onItem().ifNotNull().
                transform(student -> Response.ok(student).build())
                .onItem().ifNull()
                .continueWith(Response.ok().status(Response.Status.NOT_FOUND)::build);

    }
    @DELETE
    @Path("/{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        return Panache.withTransaction( () -> Student.deleteById(id) )
                .map( deleted -> deleted ? Response.ok().status(Response.Status.NO_CONTENT).build() :
                        Response.ok().status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Path("/addStudent")
    public Uni<Response> addStudent(Student student) {
        if(student==null)
        throw new WebApplicationException("Details were not updated on request", 422);
        else{

            System.out.println(student.getName());
            return Panache.withTransaction(student::persist).replaceWith(Response.ok(student).status(Status.CREATED)::build);
        }
       
    }
   
}
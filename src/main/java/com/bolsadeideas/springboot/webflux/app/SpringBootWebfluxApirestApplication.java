package com.bolsadeideas.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;

@EnableEurekaClient
@SpringBootApplication
public class SpringBootWebfluxApirestApplication implements CommandLineRunner {

	@Autowired
	private ProductoService service;

	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApirestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();

		Categoria electronico = new Categoria("Electrónico");
		Categoria deporte = new Categoria("Deporte");
		Categoria computacion = new Categoria("Computación");
		Categoria muebles = new Categoria("Muebles");

		Flux.just(electronico, deporte, computacion, muebles).flatMap(service::saveCategoria).doOnNext(c -> {
			log.info("Categoria creada: " + c.getNombre() + ", Id: " + c.getId());
		}).thenMany(Flux.just(new Producto("TV Panasonic", 456.89, electronico),
				new Producto("Sony Camara", 177.89, electronico), new Producto("Appl iPod", 45.89, electronico),
				new Producto("Sony Notebook", 58.89, computacion),
				new Producto("Hewlett Packard impresora", 58.85, computacion),
				new Producto("Bicle Bicicleta", 178.5, deporte), new Producto("HP Notebook Omen", 57.58, computacion),
				new Producto("Mica comoda 5", 85.88, muebles), new Producto("TV Sony OLED", 99.99, electronico))
				.flatMap(producto -> {
					producto.setFecha(new Date());
					return service.save(producto);
				})).subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));
	}

}

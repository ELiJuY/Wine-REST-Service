package clase.datos;

import java.net.URL;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "seguidor")
public class Seguidor {

	private String nombre_de_usuario;

	@XmlElement(name="nombre_de_usuario")
	public String getNombre_de_usuario() {
		return nombre_de_usuario;
	}

	public Seguidor() {
		super();
	}

	public Seguidor(String nombre_de_usuario) {
		super();
		this.nombre_de_usuario = nombre_de_usuario;
	}

	public void setNombre_de_usuario(String nombre_de_usuario) {
		this.nombre_de_usuario = nombre_de_usuario;
	}

}

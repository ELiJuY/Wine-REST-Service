package clase.datos;



import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "usuario")
@XmlType(propOrder = { "nombre_de_usuario", "correo", "fecha_de_nacimiento", "href" })
@JsonbPropertyOrder({ "nombre_de_usuario", "correo", "fecha_de_nacimiento", "href" })

public class Usuario {

	private String nombre_de_usuario;
	private String fecha_nacimiento;
	private String correo;
	private Link href;
	
	public void setNombre_de_usuario(String nombre_de_usuario) {
		this.nombre_de_usuario = nombre_de_usuario;
	}

	public void setFecha_de_nacimiento(String fecha_nacimiento) {
		this.fecha_nacimiento = fecha_nacimiento;
	}

	public void setHref(Link href) {
		this.href = href;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	@XmlElement(name="nombre_de_usuario")
	public String getNombre_de_usuario() {
		return nombre_de_usuario;
	}
	
	@XmlElement(name="fecha_de_nacimiento")
	public String getFecha_de_nacimiento() {
		return fecha_nacimiento;
	}
	
	@XmlElement(name="correo")
	public String getCorreo() {
		return correo;
	}
	
	@XmlElement(name="href")
	public Link getHref() {
		return href;
	}

	public Usuario (String nombre_de_usuario, String fecha_nacimiento, String correo) { 
        super();
		this.nombre_de_usuario=nombre_de_usuario;
		this.fecha_nacimiento=fecha_nacimiento;
		this.correo=correo;
	}
	
	public Usuario (String nombre_de_usuario, String fecha_nacimiento, String correo, Link href) { 
        super();
		this.nombre_de_usuario=nombre_de_usuario;
		this.fecha_nacimiento=fecha_nacimiento;
		this.correo=correo;
		this.href=href;
	}
	

	public Usuario () { 
     
	}
	
}

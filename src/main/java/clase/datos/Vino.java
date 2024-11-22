package clase.datos;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "vino")
@XmlType(propOrder = { "nombre_botella", "bodega_origen", "tipo_de_vino", "origen","anio","anada","tipos_de_uva","href"})
@JsonbPropertyOrder({ "nombre_botella", "bodega_origen", "tipo_de_vino", "origen","anio","anada","tipos_de_uva","href"})
public class Vino {

	private String nombre_botella;
	private String bodega_origen;
	private int anada;
	private String tipo_de_vino;
	private String [] tipos_de_uva;
	private String origen;
	private int anio;
	private Link href;
	private int puntuacion;

	@XmlTransient
	@JsonbTransient 
	public int getPuntuacion() {
		return puntuacion;
	}

	public void setPuntuacion(int puntuacion) {
		this.puntuacion = puntuacion;
	}

	public Vino(String nombre_botella, String bodega_origen, int anada, String tipo_de_vino, String[] tipos_de_uva,
			String origen, int anio) {
		this.nombre_botella = nombre_botella;
		this.bodega_origen = bodega_origen;
		this.anada = anada;
		this.tipo_de_vino = tipo_de_vino;
		this.tipos_de_uva = tipos_de_uva;
		this.origen = origen;
		this.anio = anio;
	}
	
	public Vino(String nombre_botella, String bodega_origen, int anada, String tipo_de_vino, String[] tipos_de_uva,
			String origen, int anio, Link href) {
		this.nombre_botella = nombre_botella;
		this.bodega_origen = bodega_origen;
		this.anada = anada;
		this.tipo_de_vino = tipo_de_vino;
		this.tipos_de_uva = tipos_de_uva;
		this.origen = origen;
		this.anio = anio;
		this.href = href;
	}
	
	public Vino() {

	}

	@XmlElement(name="nombre_botella")
	public String getNombre_botella() {
		return nombre_botella;
	}


	public void setNombre_botella(String nombre_botella) {
		this.nombre_botella = nombre_botella;
	}

	@XmlElement(name="bodega_origen")
	public String getBodega_origen() {
		return bodega_origen;
	}


	public void setBodega_origen(String bodega_origen) {
		this.bodega_origen = bodega_origen;
	}

	@XmlElement(name="anada")
	public int getAnada() {
		return anada;
	}


	public void setAnada(int anada) {
		this.anada = anada;
	}

	@XmlElement(name="tipo_de_vino")
	public String getTipo_de_vino() {
		return tipo_de_vino;
	}


	public void setTipo_de_vino(String tipo_de_vino) {
		this.tipo_de_vino = tipo_de_vino;
	}
    
	@XmlElementWrapper(name="tipos_de_uva")
	@XmlElement(name="tipo_de_uva")
	public String[] getTipos_de_uva() {
		return tipos_de_uva;
	}


	public void setTipos_de_uva(String[] tipos_de_uva) {
		this.tipos_de_uva = tipos_de_uva;
	}

	@XmlElement(name="origen")
	public String getOrigen() {
		return origen;
	}


	public void setOrigen(String origen) {
		this.origen = origen;
	}

	@XmlElement(name="anio")
	public int getAnio() {
		return anio;
	}


	public void setAnio(int anio) {
		this.anio = anio;
	}
	
	@XmlElement(name="href")
	public Link getHref() {
		return href;
	}
	
	public void setHref(Link href) {
		this.href = href;
	}

}

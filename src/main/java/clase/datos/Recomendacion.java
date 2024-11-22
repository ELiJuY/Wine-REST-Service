package clase.datos;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "recomendacion")
@JsonbPropertyOrder({ "usuario", "ultimos_vinos", "mejores_vinos", "mejores_vinos_amigos" })
public class Recomendacion {
    private Usuario usuario;
    private Vino [] ultimos_vinos;
    private Vino [] mejores_vinos;
    private Vino [] mejores_vinos_amigos;
	
    public Recomendacion(Usuario usuario, Vino[] ultimos_vinos, Vino[] mejores_vinos, Vino[] mejores_vinos_amigos) {
		this.usuario = usuario;
		this.ultimos_vinos = ultimos_vinos;
		this.mejores_vinos = mejores_vinos;
		this.mejores_vinos_amigos = mejores_vinos_amigos;
	}
    
    public Recomendacion() {

	}

    @XmlElement(name="usuario")
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
   
	@XmlElement(name="ultimos_vinos")
	public Vino[] getUltimos_vinos() {
		return ultimos_vinos;
	}

	public void setUltimos_vinos(Vino[] ultimos_vinos) {
		this.ultimos_vinos = ultimos_vinos;
	}
	@XmlElement(name="mejores_vinos")
	public Vino[] getMejores_vinos() {
		return mejores_vinos;
	}

	public void setMejores_vinos(Vino[] mejores_vinos) {
		this.mejores_vinos = mejores_vinos;
	}
	
	@XmlElement(name="mejores_vinos_amigos")
	public Vino[] getMejores_vinos_amigos() {
		return mejores_vinos_amigos;
	}

	public void setMejores_vinos_amigos(Vino[] mejores_vinos_amigos) {
		this.mejores_vinos_amigos = mejores_vinos_amigos;
	}


}

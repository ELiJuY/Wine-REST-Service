package clase.datos;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.ArrayList;

@XmlRootElement(name = "usuarios")
public class Usuarios {
    private List<Usuario> usuarios;

    public Usuarios() {
        this.usuarios = new ArrayList<>();
    }

    @XmlElementWrapper(name="lista_usuarios")
    @XmlElement(name="usuario")
    public List<Usuario> getLista_usuarios() {
        return usuarios;
    }
}

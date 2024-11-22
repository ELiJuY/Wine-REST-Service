package clase.recursos;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.naming.NamingContext;
import clase.datos.*;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/usuarios")
public class UsuariosRecurso {

	@Context
	private UriInfo uriInfo;
	
	private DataSource ds;
	private Connection conn;
	private DBManager dbManager;
	final static int CINCO_MEJORES = 1;
	final static int CINCO_ULTIMOS = 2;
	final static int CINCO_SEGUIDOR = 3;


	public UsuariosRecurso() {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			NamingContext envCtx = (NamingContext) ctx.lookup("java:comp/env");

			ds = (DataSource) envCtx.lookup("jdbc/Vinos");
			conn = ds.getConnection();
			dbManager = new DBManager(conn);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response createUsuario(Usuario usuario) {
		Response menorDeEdad = comprobarEdad (usuario.getFecha_de_nacimiento());
		if (menorDeEdad!= null) 
			return menorDeEdad;
		
		String sql = "INSERT INTO `Vinos`.`Usuarios` (`nombre_de_usuario`, `fecha_nacimiento`, `correo`) " + "VALUES (?, ?, ?);";
		ArrayList<Object> params = dbManager.createArrayListUsuario (usuario.getNombre_de_usuario(), usuario.getFecha_de_nacimiento(), usuario.getCorreo());
		int res = dbManager.update(sql, params);
		
		Response usarioOCorreoDuplicado = comprobarErrores(res, usuario);
		if (usarioOCorreoDuplicado!=null) 
			return usarioOCorreoDuplicado;
		
		else if (res != -1) {
			String location = uriInfo.getAbsolutePath() + "/" + usuario.getNombre_de_usuario();
			return Response.status(Response.Status.CREATED).entity(usuario).header("Location", location).header("Content-Location", location).build();
		}
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo crear el usuario").build();
	}

	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response getUsuarios(@QueryParam("offset") @DefaultValue("1") String offset,
			@QueryParam("count") @DefaultValue("10") String count,                                
			@QueryParam("pattern") String pattern) {
		try {
			int off = Integer.parseInt(offset);
			int c = Integer.parseInt(count);
			String sql;
			if (pattern != null && !pattern.isEmpty()) {
                sql = "SELECT * FROM Usuarios WHERE nombre_de_usuario LIKE ? ORDER BY nombre_de_usuario LIMIT ?,?;";
                pattern+="%";
            } else {
                sql = "SELECT * FROM Usuarios ORDER BY nombre_de_usuario LIMIT ?,?;";
            }
		    ArrayList<Object> params = dbManager.createArrayListUsuario (pattern, off-1, c);
			ResultSet rs = dbManager.query(sql, params);
			Usuarios users = new Usuarios();
			List<Usuario> usuarios = users.getLista_usuarios();
			
			while (rs.next()) {
					usuarios.add(new Usuario (rs.getString("nombre_de_usuario"),rs.getString("fecha_nacimiento"),rs.getString("correo"),
							new Link (uriInfo.getBaseUri() + "usuarios/" + rs.getString("nombre_de_usuario"))));
			}
			return Response.status(Response.Status.OK).entity(users).build();
		} catch (NumberFormatException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("No se pudieron convertir los índices a números").build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
	
	@PUT
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{nombre_usuario}")
	public Response updateUsuario(@PathParam("nombre_usuario") String nombre_usuario, Usuario usuario) {

		Response usuarioNoExiste = dbManager.comprobarUsuarioExiste(nombre_usuario);
		if (usuarioNoExiste != null)
			return usuarioNoExiste;
		
		Response menorDeEdad = comprobarEdad (usuario.getFecha_de_nacimiento());
		if (menorDeEdad!= null) 
			return menorDeEdad;
		
		String sql = "UPDATE `Vinos`.`Usuarios` SET "
				+ "`nombre_de_usuario`=?,`fecha_nacimiento`=?, `correo`=? WHERE nombre_de_usuario=?;";
	    ArrayList <Object> params = dbManager.createArrayListUsuario (usuario.getNombre_de_usuario(), usuario.getFecha_de_nacimiento(), usuario.getCorreo(), nombre_usuario);
		int res = dbManager.update(sql, params);
		
		Response usarioOCorreoDuplicado = comprobarErrores(res, usuario);
		if (usarioOCorreoDuplicado!=null) 
			return usarioOCorreoDuplicado;
		
		String location = uriInfo.getBaseUri() + "usuarios/" + usuario.getNombre_de_usuario();
		return Response.status(Response.Status.OK).entity(usuario).header("Content-Location", location).build();			
	}
	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{nombre_usuario}")
	public Response getUsuario(@PathParam("nombre_usuario") String nombre_usuario) {
		try {

			String sql = "SELECT * FROM Usuarios WHERE nombre_de_usuario = ? ";
		    ArrayList<Object> params = dbManager.createArrayListUsuario (nombre_usuario);
			ResultSet rs = dbManager.query(sql, params);
			
			if (!rs.next()) {
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario no registrado").build();
			}
			Usuario user = usuarioFromRS(rs);
			return Response.status(Response.Status.OK).entity(user).build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
	
	@DELETE
	@Path("{nombre_usuario}")
	public Response deleteUsuario(@PathParam("nombre_usuario") String nombre_usuario) {
		String sql = "DELETE FROM `Vinos`.`Usuarios` WHERE `nombre_de_usuario`= ?;";
	    ArrayList<Object> params = dbManager.createArrayListUsuario (nombre_usuario);
		int res = dbManager.update(sql, params);
		if (res==0) {
			return Response.status(Response.Status.NOT_FOUND).entity("Usuario no registrado").build();		
		}
		else if (res != -1)
			return Response.status(Response.Status.NO_CONTENT).build();
		
		else
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo eliminar el usuario\n").build();
	}
	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{nombre_usuario}/recomendacion")
	public Response getRecomendacion(@PathParam("nombre_usuario") String nombre_usuario) {
		try {
			//INFO DEL USUARIO
			String sql = "SELECT * FROM Usuarios WHERE nombre_de_usuario = ? ";
		    ArrayList<Object> params = dbManager.createArrayListUsuario (nombre_usuario);
			ResultSet rs = dbManager.query(sql, params);
			
			if (!rs.next()) {
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario no registrado").build();
			}
			Usuario user = usuarioFromRS(rs);
			
			//5 MEJORES VINOS DEL USUARIO
			sql = "SELECT * FROM Vinos WHERE nombre_de_usuario = ? ORDER BY puntuacion DESC LIMIT 0,5";
			params = dbManager.createArrayList(0, nombre_usuario);
			Vino [] cincoMejoresUsuario = vinoFromDB(sql,params,nombre_usuario,CINCO_MEJORES);
			
			//5 ULTIMO VINOS DEL USUARIO
			sql = "SELECT * FROM Vinos WHERE nombre_de_usuario = ? ORDER BY fecha_adicion DESC LIMIT 0,5";
			params = dbManager.createArrayList(0, nombre_usuario);
			Vino [] cincoUltimosUsuario = vinoFromDB(sql,params,nombre_usuario,CINCO_ULTIMOS);
		
			//5 MEJORES VINOS ENTRE TODOS LOS SEGUIDORES
			sql = "SELECT nombre_de_usuario_seguidor FROM Seguidores WHERE nombre_de_usuario_seguido = ?";
			params = dbManager.createArrayList(0, nombre_usuario);
			ResultSet seguidores= dbManager.query(sql, params);
			LinkedList <Vino> mejoresVinosSeguidores = new LinkedList <Vino> ();
			int menorNota = Integer.MIN_VALUE;
			int nVinos = 0;
			while (seguidores.next()) {
				sql = "SELECT * FROM Vinos WHERE nombre_de_usuario = ? ORDER BY puntuacion DESC LIMIT 0,5";
				params = dbManager.createArrayList(0, seguidores.getString("nombre_de_usuario_seguidor"));
				Vino [] vinosSeguidor = vinoFromDB(sql,params,nombre_usuario,CINCO_SEGUIDOR);
				for (Vino vino : vinosSeguidor) {
					if (vino!= null && (vino.getPuntuacion() > menorNota || nVinos < 5)) {
						menorNota = insertarNuevoVinoOrdenado(mejoresVinosSeguidores, vino);
						nVinos++;
					}
					else 
						break;
				}
			}
			Vino [] cincoMejoresVinosSeguidores = convertirLinkedListToArray(mejoresVinosSeguidores);
			
			Recomendacion recomendacion = new Recomendacion (user,cincoUltimosUsuario,cincoMejoresUsuario,cincoMejoresVinosSeguidores);
			
			return Response.status(Response.Status.OK).entity(recomendacion).build();
			
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BD").build();
		}
	}
	
	private Vino [] vinoFromDB (String sql, ArrayList<Object> params, String nombre_usuario, int idOperacion) {
		try {
			ResultSet rs = dbManager.query(sql, params);
			Vino [] cincoMejores = new Vino[5];
			int i = 0;
			for (; rs.next() && i < 5; i++) {
				if (idOperacion != CINCO_ULTIMOS && rs.getInt("puntuacion") == DBManager.NULL_SENTINEL_INT)
					break;

				String queryTipoUva = "SELECT TU.nombre AS tipo_uva FROM Tipos_Uva TU "+
						"INNER JOIN Vino_Tipo_Uva VTU ON TU.id=VTU.id_tipo_uva " +
						"WHERE VTU.id_vino = ?;";
				params = dbManager.createArrayList (DBManager.NO_USUARIO, rs.getString("id"));
				ResultSet rsTiposDeUva= dbManager.query(queryTipoUva, params);
				ArrayList <String> tiposDeUvaArrayList = new ArrayList <String> ();
				while (rsTiposDeUva.next()) {
					tiposDeUvaArrayList.add(rsTiposDeUva.getString(1));
				}
				String [] tiposDeUva= new String [tiposDeUvaArrayList.size()];
				for (int j = 0; j < tiposDeUvaArrayList.size(); j++) {
					tiposDeUva[j] = tiposDeUvaArrayList.get(j);
				}
				
				cincoMejores [i] = new Vino(rs.getString("nombre_botella") , rs.getString("pertenece_a_bodega") , rs.getInt("anada") , rs.getString("tipo_de_vino") , tiposDeUva,
						rs.getString("origen"), rs.getInt("ano"), new Link (uriInfo.getBaseUri() + "usuarios/" + rs.getString("nombre_de_usuario") + "/vinos/" + rs.getInt("id")));
				
				if (idOperacion != CINCO_ULTIMOS)
					cincoMejores[i].setPuntuacion(rs.getInt("puntuacion"));
				

			}
			//Borrar los null del array si no habia suficientes vinos
			if (i < 5) {
				Vino [] mejoresSinNull = new Vino [i];
				for (int cont = 0; cont<i; cont++) {
					mejoresSinNull [cont] = cincoMejores [cont];
				}
				cincoMejores = mejoresSinNull;
			}
			
			return cincoMejores;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
    // Método para insertar un nuevo vino en la lista de forma ordenada y eliminar el último elemento si la lista está llena
    public static int insertarNuevoVinoOrdenado(LinkedList<Vino> listaVinos, Vino nuevoVino) {
        // Agrega el nuevo vino a la lista
        listaVinos.add(nuevoVino);
        
        // Ordena la lista de vinos por puntuación en orden descendente
        listaVinos.sort(Comparator.comparingInt(Vino::getPuntuacion).reversed());
        
        // Si la lista tiene más de 5 elementos, elimina el último elemento
        if (listaVinos.size() > 5) {
            listaVinos.removeLast();
        }
        return listaVinos.getLast().getPuntuacion();
    }
    
    public static Vino[] convertirLinkedListToArray(LinkedList<Vino> listaVinos) {
        // Crea un nuevo array del tamaño de la lista
        Vino[] arrayVinos = new Vino[listaVinos.size()];
        
        // Copia los elementos de la lista al array
        for (int i = 0; i < listaVinos.size(); i++) {
            arrayVinos[i] = listaVinos.get(i);
        }
        
        return arrayVinos;
    }
	
	private static Response comprobarErrores(int res, Usuario usuario) {
		if (res == DBManager.USUARIO_EN_USO) {
			return Response.status(Response.Status.BAD_REQUEST).entity("El nombre de usuario \"" + usuario.getNombre_de_usuario() + "\" ya esta en uso. Elija otro.").build();
		}
		else if (res == DBManager.CORREO_EN_USO) {
			return Response.status(Response.Status.BAD_REQUEST).entity("El correo electronico \"" + usuario.getCorreo() + "\" ya esta en uso. Elija otro.").build();
		}
		else return null;
	}
	
	private static Response comprobarEdad (String fecha_nacimiento) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate fechaNacimiento = LocalDate.parse(fecha_nacimiento, formatter);
		LocalDate fechaActual = LocalDate.now();
		int edad = fechaActual.getYear() - fechaNacimiento.getYear();
		int mes = fechaActual.getMonth().getValue() - fechaNacimiento.getMonth().getValue();
		int dia = fechaActual.getDayOfMonth() - fechaNacimiento.getDayOfMonth();
        if (edad < 18 || (edad == 18 && mes < 0) || (edad==18 && mes==0 && dia < 0)) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Debes tener al menos 18 años de edad.").build();
        }
        else return null;
	}
	
	private static Usuario usuarioFromRS(ResultSet rs) throws SQLException {
		Usuario usuario = new Usuario(rs.getString("nombre_de_usuario"), rs.getDate("fecha_nacimiento").toString(), rs.getString("correo"));
		return usuario;
	}
}


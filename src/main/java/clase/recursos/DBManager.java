package clase.recursos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import clase.datos.Link;
import clase.datos.Vino;
import clase.datos.Vinos;
import jakarta.ws.rs.core.Response;

public class DBManager {
	
	final static String NULL_SENTINEL_VARCHAR = "NULL";
	final static int NULL_SENTINEL_INT = -1;
	final static java.sql.Date NULL_SENTINEL_DATE = java.sql.Date.valueOf("1900-01-01");
	final static String NULL_PATTERN = "null%";
	final static int NO_USUARIO = Integer.MAX_VALUE;
	final static int USUARIO_EN_USO = -2;
	final static int CORREO_EN_USO = -3;
	final static int NOMBRE_UVA_EXISTENTE = -4;
	final static int TIPO_UVA_DUPLICADO = -5;
	final static int VINO_DUPLICADO = -6;
	final static int SEGUIDOR_YA_EXISTE = -7;

	private Connection conn;
	
	public DBManager (Connection conn) {
		this.conn = conn;
	}

	public ResultSet query(String sql, ArrayList<Object> a) {

		try {
			
			PreparedStatement statement = conn.prepareStatement(sql); //Se prepara un statement con la sentencia SQL
            int result = setParams(statement, a); //Se rellenan los parametros de la sentencia SQL con el contenido del array
            if (result!=-1) { //No se ha producido ningun error al anadir los parametros
            	ResultSet resultSet = statement.executeQuery();
            	return resultSet;	            
            }
            
        } catch (SQLException e) {
        	//System.out.println(e.getMessage());
        }	 

	    return null; //En caso de excepcion devuelve null
	}
	
	public int update(String sql, ArrayList<Object> a) {

		PreparedStatement statement =null;
		try {
			
            statement = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS); //Se prepara un statement con la sentencia SQL
            int result = setParams(statement, a); //Se rellenan los parametros de la sentencia SQL con el contenido del array
            if (result!=-1) { //No se ha producido ningun error
            	int filasAfectadas = statement.executeUpdate(); //Se realiza el update con la sentencia SQL completa
            	ResultSet generatedID = statement.getGeneratedKeys();
    			if (generatedID.next()) {
    				return generatedID.getInt(1); //Si se generado un ID se devuelve el mismo (Usado en la clase vinos)
    			}
	            statement.close();
	            return filasAfectadas;//Devuelve el numero de filas afectadas
            }
        } catch (SQLException e) {
        	//System.out.println(e.getErrorCode()+e.getMessage());
        	if (e.getErrorCode() == 1062 || e.getErrorCode()==1761) { // Código de error para violación de restricción UNIQUE en MySQL
                String constraintName = getConstraintName(e); // Obtener el nombre de la restricción UNIQUE que causó el error
                if (e.getErrorCode()==1761 || (constraintName != null && constraintName.equals("Usuarios.PRIMARY"))) {
                    // Si la violación de la restricción UNIQUE se debe a la columna 'nombre_de_usuario'
                    return USUARIO_EN_USO;
                } else if (constraintName != null && constraintName.equals("Usuarios.correo")) {
                    // Si la violación de la restricción UNIQUE se debe a la columna 'correo'
                    return CORREO_EN_USO;
                } 
                else if (constraintName != null && constraintName.equals("Tipos_Uva.nombre")) {
                    // Si la violación de la restricción UNIQUE se debe a la columna 'nombre' (nombre tipo de uva ya anadido previamente)
                	return NOMBRE_UVA_EXISTENTE;
                }    
                else if (constraintName != null && constraintName.equals("Vino_Tipo_Uva.id_vino")) {
                    // Si la violación de la restricción UNIQUE se debe a un vino con dos tipos de uva iguales
                	return TIPO_UVA_DUPLICADO;
                }             
                else if (constraintName != null && constraintName.equals("Vinos.nombre_botella")) {
                    // Si la violación de la restricción UNIQUE se debe a un vino con todos los atributos iguales
                	return VINO_DUPLICADO;
                }
                else if (constraintName != null && constraintName.equals("Seguidores.PRIMARY")) {
                	return SEGUIDOR_YA_EXISTE;
                }
            }
        }
        	
            finally {
        		try { if (!statement.isClosed()) statement.close();}
            	catch (SQLException e) { e.printStackTrace();}
        }
	return -1; // Hubo una excepcion
	}
	
	private int setParams(PreparedStatement statement, ArrayList<Object> a) {
        if (a != null && !a.isEmpty()) { //El arrayList no esta vacio
            
        	int i=1;

        	for (Object param : a) {
                try {
                	//En caso de que el dato del ArrayList sea nulo, se anade un null al statement
	                if (param==null)
	                	continue;
	                else {
	                	String clase = param.getClass().getName();
		                //Si el dato del ArrayList no es nulo se analiza su tipo de dato y se anade al statement
	                	switch (clase) {
		                case "java.lang.String":
		                	statement.setString(i, ((String) param));
		                    break;
		                case "java.lang.Integer":
		                    statement.setInt(i, (int) param);
		                    break;
		                case "java.sql.Date":
		                	statement.setDate(i, (java.sql.Date) param);
		                    break;
		                //Para los Boolean, se sustituye true por 1 y false por 0
		                case "java.lang.Boolean":
		                	statement.setInt(i, (boolean) param? 1:0);
		                	break;
		                default:
		                   throw new SQLException("El tipo de parametro "+ clase +" no es valido.");
		                }
	                }
                } catch (SQLException e) {
                	return -1; //Si ha habido alguna excepcion el metodo devuelve -1
                }
            	i++;
        	}
            return 0; //Si el metodo termina sin excepciones se devuelve 0
        }
        return -1; //Si el arrayList es null o vacio se devuelve -1
	}
	
    public ArrayList<Object> createArrayList(int nUsuario, Object... elementos) {
        ArrayList<Object> arrayList = new ArrayList<>();
        int cont = 0;
        for (Object elemento : elementos) {
        	String clase = "";
        	if (elemento != null)
        		clase = elemento.getClass().getName();
        	if (clase.equals("java.lang.String") && cont != nUsuario)
        		arrayList.add(((String)elemento).toUpperCase());
        	else
        		arrayList.add(elemento);
        	
        	cont++;
            
        }
        return arrayList;
    }
    
    public ArrayList<Object> createArrayListUsuario(Object... elementos) {
        ArrayList<Object> arrayList = new ArrayList<>();
        for (Object elemento : elementos) {
        		arrayList.add(elemento);            
        }
        return arrayList;
    }
    
    private String getConstraintName(SQLException e) {
        String constraintName = null;
        if (e.getMessage().contains("Duplicate entry")) {
            String msg = e.getMessage();
            int startIndex = msg.indexOf("for key '") + 9;
            int endIndex = msg.indexOf("'", startIndex);
            constraintName = msg.substring(startIndex, endIndex);
        }
        return constraintName;
    }
    
    public Response comprobarUsuarioExiste (String nombre_usuario) {
     	try {
	     	String usuarioExiste = "SELECT * FROM Usuarios WHERE nombre_de_usuario = ? ";
	        ArrayList<Object> params = createArrayListUsuario (nombre_usuario);
	    	ResultSet rsUsuario = query(usuarioExiste, params);
	    	
			if (!rsUsuario.next()) {
				return Response.status(Response.Status.NOT_FOUND).entity("El usuario \"" +nombre_usuario+ "\" no esta registrado.").build();
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
     	
    	return null;
    }
    
    
    public Response comprobarVinoExiste (int id_vino) {
     	try {
	     	String vinoExiste = "SELECT * FROM Vinos WHERE id = ? ";
	        ArrayList<Object> params = createArrayListUsuario (id_vino);
	    	ResultSet rsUsuario = query(vinoExiste, params);
	    	
			if (!rsUsuario.next()) {
				return Response.status(Response.Status.NOT_FOUND).entity("El vino \"" +id_vino+ "\" no existe.").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
     	
    	return null;
    }
    public Response vinoPerteneceAUsuario (String nombre_usuario,int id_vino) {
      	try {
      		String sql = "SELECT * FROM Usuarios U INNER JOIN Vinos V ON V.nombre_de_usuario = U.nombre_de_usuario "
    			+ "WHERE U.nombre_de_usuario = ? AND `id`= ?;";
	        ArrayList<Object> params = createArrayListUsuario (nombre_usuario,id_vino);
	        ResultSet rsUsuario = query(sql, params);
	    	
	  
    		if (!rsUsuario.next()) {
    			return Response.status(Response.Status.BAD_REQUEST).entity("El vino \""+id_vino+"\" no pertenece al usuario \"" +nombre_usuario+ "\".").build();
    		}
    		
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
      	return null;
    }
    
    public Response notaExistente (int nota, int id_vino) {
      	try {
      		String sql = "SELECT puntuacion FROM Vinos WHERE id = ?;";
	        ArrayList<Object> params = createArrayListUsuario (id_vino);
	        ResultSet rsUsuario = query(sql, params);
	    	
    		if (rsUsuario.next()) {
    			if (rsUsuario.getInt(1) != NULL_SENTINEL_INT)
    				return Response.status(Response.Status.BAD_REQUEST).entity("El vino \""+id_vino+"\" ya tiene una puntuacion asignada.").build();
    			
    			else return null;

    		}
    		
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
      	return null;
    }
    
    public Response comprobarNotaValida (int nota) {
    	if (nota<0 || nota>10) {
			return Response.status(Response.Status.BAD_REQUEST).entity("La nota debe ser un numero entero de 0 a 10.").build();
    	}
    	return null;
    }
    
    
    public Response comprobarSeguimiento (String nombre_seguido, String nombre_seguidor) {
		try {
			String sql = "SELECT nombre_de_usuario_seguidor FROM Seguidores WHERE `nombre_de_usuario_seguidor`= ? "
  				+ "AND `nombre_de_usuario_seguido`= ?;";
			ArrayList<Object> params = createArrayListUsuario (nombre_seguidor,nombre_seguido);
			ResultSet rsUsuario = query(sql, params);
    	
			if (!rsUsuario.next()) {
				return Response.status(Response.Status.BAD_REQUEST).entity("El usuario \""+nombre_seguidor+"\" no es seguidor del usuario \""+nombre_seguido+"\"").build();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
    	return null;
    }
    
    public Response getVinosFromDB(String nombre_usuario,
            int offset,
            int count,
            String fecha_prev,
            String fecha_post,
            String bodega,
            String anio_prev,
            String anio,
            String anio_post,
            String tipo_de_vino,
            String origen,
            String[] uva,
            String location) {

		try {
			StringBuilder sql = new StringBuilder("SELECT V.id, V.nombre_botella, V.pertenece_a_bodega, V.anada, V.tipo_de_vino, V.origen, V.ano "
											+ "FROM Vinos V INNER JOIN Usuarios U ON V.nombre_de_usuario = U.nombre_de_usuario WHERE V.nombre_de_usuario = ?");          
	        if (fecha_prev != null)                                                         
	            sql.append(" AND V.fecha_adicion <= ?");                                                                                                                            
	        if (fecha_post != null)                                                         
	            sql.append(" AND V.fecha_adicion >= ?");                                                                                                                                          
	        if (bodega != null)                                                                 
	            sql.append(" AND V.pertenece_a_bodega = ?");                                                          
	        if (anio_prev != null)                                                               
	            sql.append(" AND V.ano <= ?");                                                                  
	        if (anio != null)                                                               
	            sql.append(" AND V.ano = ?");                                                                   
	        if (anio_post != null)                                                                 
	            sql.append(" AND V.ano >= ?");                                                                     
	        if (tipo_de_vino != null)                                                                 
	            sql.append(" AND V.tipo_de_vino = ?");                                                
	        if (origen != null)                                                               
	            sql.append(" AND V.origen = ?");  
	        
	        sql.append(" LIMIT ?,?;");
	        ArrayList <Object> params = createArrayList (0,nombre_usuario,fecha_prev,fecha_post,bodega,anio_prev,anio,anio_post,tipo_de_vino,origen,offset,count);
			ResultSet rs = query(sql.toString(), params);
			Vinos vinos = new Vinos();
			ArrayList<Vino> lista_vinos = vinos.getLista_vinos();
			
			while (rs.next()) {
				String queryTipoUva = "SELECT TU.nombre AS tipo_uva FROM Tipos_Uva TU "+
						"INNER JOIN Vino_Tipo_Uva VTU ON TU.id=VTU.id_tipo_uva " +
						"WHERE VTU.id_vino = ?;";
				params = createArrayList (DBManager.NO_USUARIO, rs.getString("id"));
				ResultSet rsTiposDeUva= query(queryTipoUva, params);
				ArrayList <String> tiposDeUvaArrayList = new ArrayList <String> ();
				while (rsTiposDeUva.next()) {
					tiposDeUvaArrayList.add(rsTiposDeUva.getString(1));
				}
				String [] tiposDeUva= new String [tiposDeUvaArrayList.size()];
				for (int i = 0; i < tiposDeUvaArrayList.size(); i++) {
					tiposDeUva[i] = tiposDeUvaArrayList.get(i);
				}
				
				//Se comprueba que el vino tenga todos los tipos de uva especificados en la URI antes de anadirlo
				boolean existe = true;
				if (uva != null) {
					int cont = 0;
					for (String tipoUva : uva) { 
						existe = false;
						for (int i = 0; i < tiposDeUvaArrayList.size() && !existe; i++) {
							if (tipoUva.toUpperCase().equals(tiposDeUva[i])) {
								existe=true;
								cont++;
							}
						}
					}
					existe = cont == uva.length ? true : false;
				}
				
				if (existe) {
					lista_vinos.add(new Vino (rs.getString("nombre_botella"), rs.getString("pertenece_a_bodega"), rs.getInt("anada"),
							rs.getString("tipo_de_vino"), tiposDeUva, rs.getString("origen"), rs.getInt("ano"), 
							new Link (/*uriInfo.getBaseUri() + "usuarios/" + nombre_usuario + "/vinos/"*/location + rs.getString("id"))));
				}
			}
			return Response.status(Response.Status.OK).entity(vinos).build();
	} catch (SQLException e) {
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
	}
    }
	
    
}

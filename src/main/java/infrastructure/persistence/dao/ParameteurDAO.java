package infrastructure.persistence.dao;

import domain.repository.ParameteurRepository;
import infrastructure.persistence.factory.DatabaseConnectionFactory;
import domain.entity.ParameteurEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ParameteurDAO implements ParameteurRepository {

    @Override
    public List<ParameteurEntity> verifyAlerts(String macAddress) {
        String sql = """
            SELECT
            	s.mac_address AS 'Endereço MAC do servidor',
                c.nome AS 'Nome do componente de hardware',
            	p.alerta_critico AS 'Alerta crítico',
                p.alerta_normal AS 'Alerta normal',
                s.nome as 'Nome'
            FROM
            	servidor s
            INNER JOIN
            	parametro p
            ON
            	s.id_servidor = p.fk_servidor
            INNER JOIN
            	componente c
            ON
            	p.fk_componente = c.id_componente
            WHERE
            	mac_address = ?;
        """;

        List<ParameteurEntity> parameteurs = new ArrayList<>();

        try (
                Connection connection = DatabaseConnectionFactory.createConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {

            pstmt.setString(1, macAddress);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ParameteurEntity parameteur = new ParameteurEntity();

                    parameteur.setNormalAlert(rs.getInt("Alerta normal"));
                    parameteur.setCriticAlert(rs.getInt("Alerta crítico"));
                    parameteur.setMacAddress(rs.getString("Endereço MAC do servidor"));
                    parameteur.setComponent(rs.getString("Nome do componente de hardware"));

                    parameteurs.add(parameteur);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameteurs;
    }


}

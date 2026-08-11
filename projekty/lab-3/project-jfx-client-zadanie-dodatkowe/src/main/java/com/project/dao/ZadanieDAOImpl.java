package com.project.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.project.datasource.DataSource;
import com.project.model.Zadanie;

public class ZadanieDAOImpl implements ZadanieDAO {

    @Override
    public List<Zadanie> findZadaniaProjektu(Integer projektId) {
        List<Zadanie> zadania = new ArrayList<>();
        String query = "SELECT * FROM zadanie WHERE projekt_id = ? ORDER BY kolejnosc ASC";
        
        try (Connection connect = DataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(query)) {
            
            prepStmt.setInt(1, projektId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    Zadanie zadanie = new Zadanie();
                    zadanie.setZadanieId(rs.getInt("zadanie_id"));
                    zadanie.setProjektId(rs.getInt("projekt_id"));
                    zadanie.setNazwa(rs.getString("nazwa"));
                    zadanie.setOpis(rs.getString("opis"));
                    zadanie.setKolejnosc(rs.getInt("kolejnosc"));
                    zadanie.setDataczasUtworzenia(rs.getObject("dataczas_utworzenia", LocalDateTime.class));
                    zadania.add(zadanie);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return zadania;
    }

    @Override
    public void setZadanie(Zadanie zadanie) {
        boolean isInsert = zadanie.getZadanieId() == null;
        String query = isInsert ?
            "INSERT INTO zadanie (projekt_id, nazwa, opis, kolejnosc, dataczas_utworzenia) VALUES (?, ?, ?, ?, ?)"
            : "UPDATE zadanie SET projekt_id = ?, nazwa = ?, opis = ?, kolejnosc = ?, dataczas_utworzenia = ? WHERE zadanie_id = ?";
        
        try (Connection connect = DataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            prepStmt.setInt(1, zadanie.getProjektId());
            prepStmt.setString(2, zadanie.getNazwa());
            prepStmt.setString(3, zadanie.getOpis());
            prepStmt.setInt(4, zadanie.getKolejnosc());
            if (zadanie.getDataczasUtworzenia() == null) {
                zadanie.setDataczasUtworzenia(LocalDateTime.now());
            }
            prepStmt.setObject(5, zadanie.getDataczasUtworzenia());
            
            if (!isInsert) {
                prepStmt.setInt(6, zadanie.getZadanieId());
            }
            
            int affectedRows = prepStmt.executeUpdate();
            if (isInsert && affectedRows > 0) {
                try (ResultSet keys = prepStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        zadanie.setZadanieId(keys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteZadanie(Integer zadanieId) {
        String query = "DELETE FROM zadanie WHERE zadanie_id = ?";
        try (Connection connect = DataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(query)) {
            
            prepStmt.setInt(1, zadanieId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
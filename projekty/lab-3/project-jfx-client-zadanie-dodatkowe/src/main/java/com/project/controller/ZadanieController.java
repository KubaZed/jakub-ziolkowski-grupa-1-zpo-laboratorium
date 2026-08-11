package com.project.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.project.dao.ZadanieDAO;
import com.project.model.Projekt;
import com.project.model.Zadanie;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class ZadanieController {
    private static final Logger logger = LoggerFactory.getLogger(ZadanieController.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Projekt projekt;
    private final ZadanieDAO zadanieDAO;
    private final ExecutorService wykonawca;
    private ObservableList<Zadanie> zadaniaList;

    @FXML
    private Button btnPowrot;
    @FXML
    private TableView<Zadanie> tblZadanie;
    @FXML
    private TableColumn<Zadanie, Integer> colZadanieId;
    @FXML
    private TableColumn<Zadanie, String> colNazwa;
    @FXML
    private TableColumn<Zadanie, String> colOpis;
    @FXML
    private TableColumn<Zadanie, Integer> colKolejnosc;
    @FXML
    private TableColumn<Zadanie, LocalDateTime> colDataUtworzenia;

    public ZadanieController(Projekt projekt, ZadanieDAO zadanieDAO, ExecutorService wykonawca) {
        this.projekt = projekt;
        this.zadanieDAO = zadanieDAO;
        this.wykonawca = wykonawca;
    }

    @FXML
    public void initialize() {
        colZadanieId.setCellValueFactory(new PropertyValueFactory<>("zadanieId"));
        colNazwa.setCellValueFactory(new PropertyValueFactory<>("nazwa"));
        colOpis.setCellValueFactory(new PropertyValueFactory<>("opis"));
        colKolejnosc.setCellValueFactory(new PropertyValueFactory<>("kolejnosc"));
        colDataUtworzenia.setCellValueFactory(new PropertyValueFactory<>("dataczasUtworzenia"));

        colDataUtworzenia.setCellFactory(column -> new TableCell<Zadanie, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(dateTimeFormatter.format(item));
                }
            }
        });

        TableColumn<Zadanie, Void> colActions = new TableColumn<>("Akcje");
        colActions.setCellFactory(column -> new TableCell<Zadanie, Void>() {
            private final GridPane pane;

            {
                Button btnEdit = new Button("Edycja");
                Button btnRemove = new Button("Usuń");

                btnEdit.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btnRemove.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                btnEdit.setOnAction(event -> edytujZadanie(getCurrentZadanie()));
                btnRemove.setOnAction(event -> usunZadanie(getCurrentZadanie()));

                pane = new GridPane();
                pane.setAlignment(Pos.CENTER);
                pane.setHgap(5);
                pane.setVgap(5);
                pane.setPadding(new Insets(2, 2, 2, 2));
                pane.add(btnEdit, 0, 0);
                pane.add(btnRemove, 1, 0);
            }

            private Zadanie getCurrentZadanie() {
                int index = this.getTableRow().getIndex();
                return this.getTableView().getItems().get(index);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        tblZadanie.getColumns().add(colActions);

        zadaniaList = FXCollections.observableArrayList();
        tblZadanie.setItems(zadaniaList);

        pobierzZadania();
    }

    private void pobierzZadania() {
        wykonawca.execute(() -> {
            try {
                List<Zadanie> lista = zadanieDAO.findZadaniaProjektu(projekt.getProjektId());
                Platform.runLater(() -> {
                    zadaniaList.clear();
                    zadaniaList.addAll(lista);
                });
            } catch (RuntimeException e) {
                logger.error("Błąd podczas pobierania listy zadań", e);
                Platform.runLater(() -> showError("Błąd", "Nie udało się pobrać zadań dla projektu."));
            }
        });
    }

    @FXML
    public void onActionBtnDodaj(ActionEvent event) {
        Zadanie noweZadanie = new Zadanie();
        noweZadanie.setProjektId(projekt.getProjektId());
        edytujZadanie(noweZadanie);
    }

    @FXML
    private void onActionBtnPowrot(ActionEvent event) {
        Stage stage = (Stage) btnPowrot.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private void edytujZadanie(Zadanie zadanie) {
        Dialog<Zadanie> dialog = new Dialog<>();
        dialog.setTitle("Edycja Zadania");
        if (zadanie.getZadanieId() != null) {
            dialog.setHeaderText("Edycja danych zadania");
        } else {
            dialog.setHeaderText("Dodawanie nowego zadania");
        }
        dialog.setResizable(true);

        Label lblId = getRightLabel("Id: ");
        Label lblNazwa = getRightLabel("Nazwa: ");
        Label lblOpis = getRightLabel("Opis: ");
        Label lblKolejnosc = getRightLabel("Kolejność: ");

        Label txtId = new Label(zadanie.getZadanieId() != null ? zadanie.getZadanieId().toString() : "");
        TextField txtNazwa = new TextField(zadanie.getNazwa() != null ? zadanie.getNazwa() : "");
        TextArea txtOpis = new TextArea(zadanie.getOpis() != null ? zadanie.getOpis() : "");
        txtOpis.setPrefRowCount(4);
        txtOpis.setWrapText(true);
        TextField txtKolejnosc = new TextField(zadanie.getKolejnosc() != null ? zadanie.getKolejnosc().toString() : "");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        grid.add(lblId, 0, 0);
        grid.add(txtId, 1, 0);
        grid.add(lblNazwa, 0, 1);
        grid.add(txtNazwa, 1, 1);
        grid.add(lblOpis, 0, 2);
        grid.add(txtOpis, 1, 2);
        grid.add(lblKolejnosc, 0, 3);
        grid.add(txtKolejnosc, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk = new ButtonType("Zapisz", ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("Anuluj", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, buttonTypeCancel);

        dialog.setResultConverter(new Callback<ButtonType, Zadanie>() {
            @Override
            public Zadanie call(ButtonType buttonType) {
                if (buttonType == buttonTypeOk) {
                    try {
                        zadanie.setNazwa(txtNazwa.getText().trim());
                        zadanie.setOpis(txtOpis.getText().trim());
                        zadanie.setKolejnosc(Integer.parseInt(txtKolejnosc.getText().trim()));
                        return zadanie;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                return null;
            }
        });

        Optional<Zadanie> result = dialog.showAndWait();
        if (result.isPresent()) {
            wykonawca.execute(() -> {
                try {
                    zadanieDAO.setZadanie(zadanie);
                    Platform.runLater(() -> {
                        if (tblZadanie.getItems().contains(zadanie)) {
                            tblZadanie.refresh();
                        } else {
                            pobierzZadania();
                        }
                    });
                } catch (RuntimeException e) {
                    logger.error("Błąd podczas zapisywania zadania", e);
                    Platform.runLater(() -> showError("Błąd", "Naruszenie unikalności kolejności lub błąd bazy!"));
                }
            });
        }
    }

    private void usunZadanie(Zadanie zadanie) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie");
        alert.setHeaderText("Usuwanie zadania");
        alert.setContentText("Czy na pewno chcesz usunąć zadanie: " + zadanie.getNazwa() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            wykonawca.execute(() -> {
                try {
                    zadanieDAO.deleteZadanie(zadanie.getZadanieId());
                    Platform.runLater(() -> zadaniaList.remove(zadanie));
                } catch (RuntimeException e) {
                    logger.error("Błąd podczas usuwania zadania", e);
                    Platform.runLater(() -> showError("Błąd", "Nie udało się usunąć zadania."));
                }
            });
        }
    }

    private Label getRightLabel(String text) {
        Label lbl = new Label(text);
        lbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER_RIGHT);
        return lbl;
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
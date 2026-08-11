package com.project.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.project.model.Projekt;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView; 
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.project.dao.ProjektDAO;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.TableCell;
import java.util.Optional;
import java.time.format.DateTimeParseException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ProjectController {
	private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
	
	//Zmienne do obsługi stronicowania i wyszukiwania
	private String search4;
	private Integer pageNo;
	private Integer pageSize;
	
	private ObservableList<Projekt> projekty;
	
//Automatycznie wstrzykiwane komponenty GUI
	@FXML
	private ChoiceBox<Integer> cbPageSizes;
	@FXML
	private TableView<Projekt> tblProjekt;
	@FXML
	private TableColumn<Projekt, Integer> colId;
	@FXML
	private TableColumn<Projekt, String> colNazwa;
	@FXML
	private TableColumn<Projekt, String> colOpis;
	@FXML
	private TableColumn<Projekt, LocalDateTime> colDataCzasUtworzenia;
	@FXML
	private TableColumn<Projekt, LocalDate> colDataOddania;
	@FXML
	private TextField txtSzukaj; 
	@FXML 
	private Button btnDalej; 
	@FXML 
	private Button btnWstecz; 
	@FXML 
	private Button btnPierwsza; 
	@FXML 
	private Button btnOstatnia;

	public ProjectController() { //Utworzeniu konstruktora jest obligatoryjne
	}
	
	private ExecutorService wykonawca;
	private ProjektDAO projektDAO;

	public ProjectController(ProjektDAO projektDAO) {
	    this.projektDAO = projektDAO;
	    this.wykonawca = Executors.newFixedThreadPool(1); // W naszej aplikacji wystarczy jeden wątek do pobierania
														  // danych. Przekazanie większej liczby takich zadań do puli
														  // jednowątkowej powoduje ich kolejkowanie i sukcesywne // wykonywanie.
	}
	
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter dateTimeFormater = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	@FXML
	public void initialize() {
	    // // Inicjalizacja kolumn tabeli wartościami z modelu Projekt
	    colId.setCellValueFactory(new PropertyValueFactory<Projekt, Integer>("projektId"));
	    colNazwa.setCellValueFactory(new PropertyValueFactory<Projekt, String>("nazwa"));
	    colOpis.setCellValueFactory(new PropertyValueFactory<Projekt, String>("opis"));
	    colDataCzasUtworzenia.setCellValueFactory(new PropertyValueFactory<Projekt, LocalDateTime>("dataCzasUtworzenia"));
	    colDataOddania.setCellValueFactory(new PropertyValueFactory<Projekt, LocalDate>("dataOddania"));

	    // Formatowanie kolumny Data utworzenia (LocalDateTime)
	    colDataCzasUtworzenia.setCellFactory(column -> new TableCell<Projekt, LocalDateTime>() {
	        @Override
	        protected void updateItem(LocalDateTime item, boolean empty) {
	            super.updateItem(item, empty);
	            if (item == null || empty) {
	                setText(null);
	                setGraphic(null);
	            } else {
	                setText(dateTimeFormater.format(item));
	            }
	        }
	    });

	    // Formatowanie kolumny Data oddania (LocalDate)
	    colDataOddania.setCellFactory(column -> new TableCell<Projekt, LocalDate>() {
	        @Override
	        protected void updateItem(LocalDate item, boolean empty) {
	            super.updateItem(item, empty);
	            if (item == null || empty) {
	                setText(null);
	                setGraphic(null);
	            } else {
	                setText(dateFormatter.format(item));
	            }
	        }
	    });

	    // Utworzenie nowej kolumny z przyciskami akcji
	    TableColumn<Projekt, Void> colEdit = new TableColumn<>("Edycja");
	    colEdit.setCellFactory(column -> new TableCell<Projekt, Void>() {
	        private final GridPane pane;

	        {
	            // Blok inicjalizujący w anonimowej klasie wewnętrznej
	            Button btnEdit = new Button("Edycja");
	            Button btnRemove = new Button("Usuń");
	            Button btnTask = new Button("Zadania");

	            btnEdit.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
	            btnRemove.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
	            btnTask.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

	            // Rejestracja zdarzenia dla przycisku Edycja
	            btnEdit.setOnAction(event -> {
	                edytujProjekt(getCurrentProjekt());
	            });

	            // Rejestracja zdarzenia dla przycisku Usuń
	            btnRemove.setOnAction(event -> {
	                usunProjekt(getCurrentProjekt());
	            });

	            // Rejestracja zdarzenia dla przycisku Zadania
	            btnTask.setOnAction(event -> {
	                // openZadanieFrame(getCurrentProjekt());
	            });

	            // Konstrukcja panelu i układu przycisków
	            pane = new GridPane();
	            pane.setAlignment(Pos.CENTER);
	            pane.setHgap(10);
	            pane.setVgap(10);
	            pane.setPadding(new Insets(5, 5, 5, 5));

	            pane.add(btnTask, 0, 0);
	            pane.add(btnEdit, 0, 1);
	            pane.add(btnRemove, 0, 2);
	        }

	        private Projekt getCurrentProjekt() {
	            int index = this.getTableRow().getIndex();
	            return this.getTableView().getItems().get(index);
	        }

	        @Override
	        protected void updateItem(Void item, boolean empty) {
	            super.updateItem(item, empty);
	            setGraphic(empty ? null : pane);
	        }
	    });

	    // Dodanie nowej kolumny do tabeli
	    tblProjekt.getColumns().add(colEdit);

	    // Ustawienie względnej szerokości poszczególnych kolumn
	    colId.setMaxWidth(5000);
	    colNazwa.setMaxWidth(10000);
	    colOpis.setMaxWidth(10000);
	    colDataCzasUtworzenia.setMaxWidth(9000);
	    colDataOddania.setMaxWidth(7000);
	    colEdit.setMaxWidth(7000);

	    // Inicjalizacja listy i powiązanie jej z TableView
	    projekty = FXCollections.observableArrayList();
	    tblProjekt.setItems(projekty);

	    				//Metoda automatycznie wywoływana przez JavaFX zaraz po wstrzyknięciu
	    search4 = "";   //wszystkich komponentów. Uwaga! Wszelkie modyfikacje komponentów
	    pageNo = 0;     //(np. cbPageSizes) trzeba realizować wewnątrz tej metody. Nigdy
	    pageSize = 10;  //nie używaj do tego celu konstruktora.
	    
	    cbPageSizes.getItems().addAll(5, 10, 20, 50, 100);
	    cbPageSizes.setValue(pageSize);

	    wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
	}
	
	//Grupa metod do obsługi przycisków
	@FXML
	private void onActionBtnSzukaj(ActionEvent event) {
	    // Pobranie tekstu z pola wyszukiwania i usunięcie białych znaków
	    search4 = txtSzukaj.getText().trim();
	    // Resetowanie numeru strony do pierwszej (indeks 0) przy nowym wyszukiwaniu
	    pageNo = 0;
	    // Asynchroniczne wywołanie pobierania danych
	    wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
	}

	@FXML
	private void onActionBtnDalej(ActionEvent event) {
	    // Inkrementacja numeru strony
	    pageNo++;
	    // Asynchroniczne pobranie kolejnej strony danych
	    wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
	}

	@FXML
	private void onActionBtnWstecz(ActionEvent event) {
	    // Warunek zabezpieczający przed przejściem poniżej pierwszej strony
	    if (pageNo > 0) {
	        pageNo--;
	        // Asynchroniczne pobranie poprzedniej strony danych
	        wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
	    }
	}
	
	@FXML
	private void onActionBtnPierwsza(ActionEvent event) {
	}
	
	@FXML
	private void onActionBtnOstatnia(ActionEvent event) {
	}
	
	@FXML
	public void onActionBtnDodaj(ActionEvent event) {
	    edytujProjekt(new Projekt());
	}
	
	private void loadPage(String search4, Integer pageNo, Integer pageSize) {
	    try {
	        final List<Projekt> projektList = new ArrayList<>();
	        if (search4 != null && !search4.isEmpty()) {
	            if (search4.matches("[0-9]+")) {
	                // Identyfikator
	                Integer id = Integer.parseInt(search4);
	                Projekt p = projektDAO.getProjekt(id);
	                if (p != null) {
	                    projektList.add(p);
	                }
	            } else if (search4.matches("^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$")) {
	                // Data
	                LocalDate data = LocalDate.parse(search4);
	                projektList.addAll(projektDAO.getProjektyWhereDataOddaniaIs(data, pageNo * pageSize, pageSize));
	            } else {
	                // Nazwa
	                projektList.addAll(projektDAO.getProjektyWhereNazwaLike(search4, pageNo * pageSize, pageSize));
	            }
	        } else {
	            // Brak filtrów - Pobierz wszystkie
	            projektList.addAll(projektDAO.getProjekty(pageNo * pageSize, pageSize));
	        }
	        
	        Platform.runLater(() -> {
	            projekty.clear();
	            projekty.addAll(projektList);
	        });
	    } catch (RuntimeException e) {
	        String errMsg = "Błąd podczas pobierania listy projektów.";
	        logger.error(errMsg, e);
	        String errDetails = e.getCause() != null ? 
	                e.getMessage() + "\n" + e.getCause().getMessage() : e.getMessage();
	        Platform.runLater(() -> showError(errMsg, errDetails));
	    }
	}

	/** Metoda pomocnicza do prezentowania użytkownikowi informacji o błędach */
	private void showError(String header, String content) {
	    Alert alert = new Alert(AlertType.ERROR);
	    alert.setTitle("Błąd");
	    alert.setHeaderText(header);
	    alert.setContentText(content);
	    alert.showAndWait();
	}

	public void shutdown() {
		// Wystarczyłoby tylko samo wywołanie metody wykonawca.shutdownNow(), ale można również, tak jak poniżej,
		// zaimplementować wersję z oczekiwaniem na zakończenie wszystkich zadań wykonywanych w puli wątków.
	    if (wykonawca != null) {
	        wykonawca.shutdown();
	        try {
	            if (!wykonawca.awaitTermination(5, TimeUnit.SECONDS)) {
	                wykonawca.shutdownNow();
	            }
	        } catch (InterruptedException e) {
	            wykonawca.shutdownNow();
	        }
	    }
	}
	
	private void edytujProjekt(Projekt projekt) {
	    Dialog<Projekt> dialog = new Dialog<>();
	    dialog.setTitle("Edycja");
	    if (projekt.getProjektId() != null) {
	        dialog.setHeaderText("Edycja danych projektu");
	    } else {
	        dialog.setHeaderText("Dodawanie projektu");
	    }
	    dialog.setResizable(true);

	    Label lblId = getRightLabel("Id: ");
	    Label lblNazwa = getRightLabel("Nazwa: ");
	    Label lblOpis = getRightLabel("Opis: ");
	    Label lblDataCzasUtworzenia = getRightLabel("Data utworzenia: ");
	    Label lblDataOddania = getRightLabel("Data oddania: ");

	    Label txtId = new Label();
	    if (projekt.getProjektId() != null) {
	        txtId.setText(projekt.getProjektId().toString());
	    }

	    TextField txtNazwa = new TextField();
	    if (projekt.getNazwa() != null) {
	        txtNazwa.setText(projekt.getNazwa());
	    }

	    TextArea txtOpis = new TextArea();
	    txtOpis.setPrefRowCount(6);
	    txtOpis.setPrefColumnCount(40);
	    txtOpis.setWrapText(true);
	    if (projekt.getOpis() != null) {
	        txtOpis.setText(projekt.getOpis());
	    }

	    Label txtDataUtworzenia = new Label();
	    if (projekt.getDataCzasUtworzenia() != null) {
	        txtDataUtworzenia.setText(dateTimeFormater.format(projekt.getDataCzasUtworzenia()));
	    }

	    DatePicker dtDataOddania = new DatePicker();
	    dtDataOddania.setPromptText("RRRR-MM-DD");
	    dtDataOddania.setConverter(new StringConverter<LocalDate>() {
	        @Override
	        public String toString(LocalDate date) {
	            return date != null ? dateFormatter.format(date) : null;
	        }
	        @Override
	        public LocalDate fromString(String text) {
	            return text == null || text.trim().isEmpty() ? null : LocalDate.parse(text, dateFormatter);
	        }
	    });

	    dtDataOddania.getEditor().focusedProperty().addListener((obsValue, oldFocus, newFocus) -> {
	        if (!newFocus) {
	            try {
	                dtDataOddania.setValue(dtDataOddania.getConverter().fromString(dtDataOddania.getEditor().getText()));
	            } catch (DateTimeParseException e) {
	                dtDataOddania.getEditor().setText(dtDataOddania.getConverter().toString(dtDataOddania.getValue()));
	            }
	        }
	    });

	    if (projekt.getDataOddania() != null) {
	        dtDataOddania.setValue(projekt.getDataOddania());
	    }

	    GridPane grid = new GridPane();
	    grid.setHgap(10);
	    grid.setVgap(10);
	    grid.setPadding(new Insets(5, 5, 5, 5));

	    grid.add(lblId, 0, 0);
	    grid.add(txtId, 1, 0);
	    grid.add(lblDataCzasUtworzenia, 0, 1);
	    grid.add(txtDataUtworzenia, 1, 1);
	    grid.add(lblNazwa, 0, 2);
	    grid.add(txtNazwa, 1, 2);
	    grid.add(lblOpis, 0, 3);
	    grid.add(txtOpis, 1, 3);
	    grid.add(lblDataOddania, 0, 4);
	    grid.add(dtDataOddania, 1, 4);

	    dialog.getDialogPane().setContent(grid);

	    ButtonType buttonTypeOk = new ButtonType("Zapisz", ButtonData.OK_DONE);
	    ButtonType buttonTypeCancel = new ButtonType("Anuluj", ButtonData.CANCEL_CLOSE);
	    dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, buttonTypeCancel);

	    dialog.setResultConverter(new Callback<ButtonType, Projekt>() {
	        @Override
	        public Projekt call(ButtonType buttonType) {
	            if (buttonType == buttonTypeOk) {
	                projekt.setNazwa(txtNazwa.getText().trim());
	                projekt.setOpis(txtOpis.getText().trim());
	                projekt.setDataOddania(dtDataOddania.getValue());
	                return projekt;
	            }
	            return null;
	        }
	    });

	    Optional<Projekt> result = dialog.showAndWait();
	    if (result.isPresent()) {
	        wykonawca.execute(() -> {
	            try {
	                projektDAO.setProjekt(projekt);
	                Platform.runLater(() -> {
	                    if (tblProjekt.getItems().contains(projekt)) {
	                        tblProjekt.refresh();
	                    } else {
	                        tblProjekt.getItems().add(0, projekt);
	                    }
	                });
	            } catch (RuntimeException e) {
	                String errMsg = "Błąd podczas zapisywania danych projektu!";
	                logger.error(errMsg, e);
	                String errDetails = e.getCause() != null ? 
	                        e.getMessage() + "\n" + e.getCause().getMessage() : e.getMessage();
	                Platform.runLater(() -> showError(errMsg, errDetails));
	            }
	        });
	    }
	}
	
	private void usunProjekt(Projekt projekt) {
	    Alert alert = new Alert(AlertType.CONFIRMATION);
	    alert.setTitle("Potwierdzenie");
	    alert.setHeaderText("Usuwanie projektu");
	    alert.setContentText("Czy na pewno chcesz usunąć projekt: " + projekt.getNazwa() + "?");

	    Optional<ButtonType> result = alert.showAndWait();
	    if (result.isPresent() && result.get() == ButtonType.OK) {
	        wykonawca.execute(() -> {
	            try {
	                // Usunięcie z bazy danych
	                projektDAO.deleteProjekt(projekt.getProjektId());
	                
	                // Aktualizacja interfejsu graficznego
	                Platform.runLater(() -> {
	                    projekty.remove(projekt);
	                });
	            } catch (RuntimeException e) {
	                String errMsg = "Błąd podczas usuwania projektu!";
	                logger.error(errMsg, e);
	                String errDetails = e.getCause() != null ? 
	                        e.getMessage() + "\n" + e.getCause().getMessage() : e.getMessage();
	                Platform.runLater(() -> showError(errMsg, errDetails));
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
}
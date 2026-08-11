package com.lab.statistics;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class MainFrame {
	private JFrame frame;
	private final WordStatisticsService statisticsService;
	
	// ścieżka do katalogu z plikami tekstowymi
	private static final String DIR_PATH = "files";
	
	// określa ile najczęściej występujących wyrazów bierzemy pod uwagę
	private final int liczbaWyrazowStatystyki;
	private final AtomicBoolean fajrant;
	private final int liczbaProducentow;
	private final int liczbaKonsumentow;
	
	// pula wątków – obiekt klasy ExecutorService, który zarządza tworzeniem
	// nowych oraz wykonuje 'recykling' zakończonych wątków
	private ExecutorService executor;
	
	// lista obiektów klasy Future, dzięki którym mamy możliwość nadzoru pracy wątków
	// producenckich np. sprawdzania czy wątek jest aktywny lub jego anulowania/przerywania
	private List<Future<?>> producentFuture;
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
					try {
						MainFrame window = new MainFrame();
						window.frame.pack();
						window.frame.setAlwaysOnTop(true);
						window.frame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
		});
	}
	
	public MainFrame() {
		liczbaWyrazowStatystyki = 10;
		fajrant = new AtomicBoolean(false);
		liczbaProducentow = 1;
		liczbaKonsumentow = 2;
		executor = Executors.newFixedThreadPool(liczbaProducentow + liczbaKonsumentow);
		producentFuture = new ArrayList<>();
		statisticsService = new WordStatisticsService();
		initialize();
	}
	
	/**
	* Initialize the contents of the frame.
	*/
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				executor.shutdownNow();
			}
		});
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fajrant.set(true);
				for (Future<?> f : producentFuture) {
					f.cancel(true);
				}
			}
		});
		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getMultiThreadedStatistics();
			}
		});
		JButton btnZamknij = new JButton("Zamknij");
		btnZamknij.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executor.shutdownNow();
				frame.dispose();
			}
		});
		panel.add(btnStart);
		panel.add(btnStop);
		panel.add(btnZamknij);
	}
	
	/**
	* Statystyka wyrazów (wzorzec PRODUCENT - KONSUMENT korzystający z kolejki blokującej)
	*/
	private void getMultiThreadedStatistics() {
		for (Future<?> f : producentFuture) {
			if (!f.isDone()) {
				JOptionPane.showMessageDialog(frame, "Nie można uruchomić nowego zadania! Przynajmniej jeden producent nadal działa!", "OSTRZEŻENIE", JOptionPane.WARNING_MESSAGE);
				return;
			}
		}
		
		fajrant.set(false);
		producentFuture.clear();
		final BlockingQueue<Optional<Path>> kolejka = new LinkedBlockingQueue<>(liczbaKonsumentow);
		final int przerwa = 60;
	
		Runnable producent = () -> {
			final String name = Thread.currentThread().getName();
			String info = String.format("PRODUCENT %s URUCHOMIONY ...", name);
			System.out.println(info);
	
			try {
				while (!Thread.currentThread().isInterrupted()) {
					if(fajrant.get()) {
						// TODO przekazanie poison pills (kolejka.put(Optional.empty());) konsumentom i zakończenia działania
						info = String.format("PRODUCENT %s – wysyłam poison pills i kończę.", name);
						System.out.println(info);
						try {
					        for (int i = 0; i < liczbaKonsumentow; i++) {
					            kolejka.put(Optional.empty());
					        }
					    } catch (InterruptedException e) {
					        System.out.printf("PRODUCENT %s przerwany podczas wysyłania poison pills%n", name);
					        Thread.currentThread().interrupt();
					    }
					    break;
					} else {
						// TODO Wyszukiwanie plików *.txt i wstawianie do kolejki ścieżki opakowanej w Optional
						Path dir = Paths.get(DIR_PATH);
	                    if (!Files.exists(dir)) {
	                        System.out.printf("Katalog %s nie istnieje!%n", DIR_PATH);
	                    } else {
	                        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.txt");
	                        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
	                            @Override
	                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	                                if (matcher.matches(file)) {
	                                    try {
	                                        Optional<Path> optPath = Optional.ofNullable(file);
	                                        kolejka.put(optPath);
	                                        System.out.printf("PRODUCENT %s dodał do kolejki: %s%n", name, file);
	                                    } catch (InterruptedException e) {
	                                        System.out.printf("PRODUCENT %s przerwany podczas put()%n", name);
	                                        Thread.currentThread().interrupt();
	                                        return FileVisitResult.TERMINATE;
	                                    }
	                                }
	                                return FileVisitResult.CONTINUE;
	                            }
	                        });
	                    }
					}
					info = String.format("Producent %s ponownie sprawdzi katalogi za %d sekund", name, przerwa);
					System.out.println(info);
					try {
						TimeUnit.SECONDS.sleep(przerwa);
					} catch (InterruptedException e) {
						info = String.format("Przerwa producenta %s przerwana!", name);
						System.out.println(info);
						if(!fajrant.get()) Thread.currentThread().interrupt();
					}
				} 
			} catch (IOException e) {
	            System.out.printf("Błąd I/O w producencie %s: %s%n", name, e.getMessage());
	        }

			String infoEnd = String.format("PRODUCENT %s SKOŃCZYŁ PRACĘ", name);
	        System.out.println(infoEnd);
	    };
		
		Runnable konsument = () -> {
			final String name = Thread.currentThread().getName();
			String info = String.format("KONSUMENT %s URUCHOMIONY ...", name);
			System.out.println(info);
			
			while (!Thread.currentThread().isInterrupted()) {
				try {
					// TODO pobieranie ścieżki (Optional<Path> optPath = kolejka.take();) i tworzenie statystyki wyrazów
					Optional<Path> optPath = kolejka.take();
	                if (!optPath.isPresent()) {
	                    System.out.printf("KONSUMENT %s otrzymał poison pill – kończy pracę.%n", name);
	                    break;
	                }
	                Path path = optPath.get();
	                System.out.printf("KONSUMENT %s przetwarza plik: %s%n", name, path);

	                Map<String, Long> countedWords =
	                        statisticsService.getLinkedCountedWords(path, liczbaWyrazowStatystyki);

	                System.out.printf("Najczęstsze słowa w pliku %s (konsument %s):%n", path, name);
	                countedWords.forEach((word, count) ->
	                        System.out.printf("%s = %d%n", word, count));
	                
				} catch (InterruptedException e) {
					info = String.format("Oczekiwanie konsumenta %s na nowy element z kolejki przerwane!", name);
					System.out.println(info);
					Thread.currentThread().interrupt();
				}
			}
			String infoEnd = String.format("KONSUMENT %s ZAKOŃCZYŁ PRACĘ", name);
	        System.out.println(infoEnd);
	    };
		
		//uruchamianie wszystkich wątków-producentów
		for (int i = 0; i < liczbaProducentow; i++) {
			Future<?> pf = executor.submit(producent);
			producentFuture.add(pf);
		}
		//uruchamianie wszystkich wątków-konsumentów
		for (int i = 0; i < liczbaKonsumentow; i++) {
			executor.execute(konsument);
		}
	}
}
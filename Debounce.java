import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Debounce {

    static String data = "";

    public static void main(String[] args) {

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://jsonplaceholder.typicode.com/comments?postId=1"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status Code: " + response.statusCode());

            if (response.statusCode() != 200) {
                System.out.println("API request failed!");
                return;
            }

            data = response.body();

        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("API Debounce Search");
            frame.setSize(650, 450);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout(10, 10));

            JTextField searchField = new JTextField();
            JTextArea resultArea = new JTextArea();
            resultArea.setEditable(false);

            frame.add(searchField, BorderLayout.NORTH);
            frame.add(new JScrollPane(resultArea), BorderLayout.CENTER);

            Timer debounce = new Timer(1500, e -> {

                String input = searchField.getText().trim();
                resultArea.setText("");

                if (input.isEmpty()) {
                    resultArea.setText("Enter a name to search.");
                    return;
                }

                
                if (input.length() < 3) {
                    resultArea.setText("Please enter at least 3 characters.");
                    return;
                }

                
                Pattern pattern = Pattern.compile("\\{[^{}]*\\}");
                Matcher matcher = pattern.matcher(data);

                boolean found = false;

                while (matcher.find()) {
                    String comment = matcher.group();

                    Pattern namePattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]*)\"");

                    Matcher nameMatcher = namePattern.matcher(comment);

                    if (nameMatcher.find()) {

                        String name = nameMatcher.group(1);

                        if (name.toLowerCase().contains(
                                input.toLowerCase())) {

                            resultArea.append(comment + "\n\n");
                            found = true;
                        }
                    }
                }

                if (!found) {
                    resultArea.setText("No matching data found!");
                }
            });

            debounce.setRepeats(false);

            searchField.getDocument().addDocumentListener(
                new DocumentListener() {

                    public void insertUpdate(DocumentEvent e) {
                        debounce.restart();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        debounce.restart();
                    }

                    public void changedUpdate(DocumentEvent e) {
                        debounce.restart();
                    }
                }
            );

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

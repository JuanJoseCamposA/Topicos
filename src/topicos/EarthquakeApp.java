package topicos;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

public class EarthquakeApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Solicitar fechas de inicio y fin al usuario
        System.out.print("Ingrese la fecha de inicio (YYYY-MM-DD): ");
        String startDate = scanner.nextLine();

        System.out.print("Ingrese la fecha final (YYYY-MM-DD): ");
        String endDate = scanner.nextLine();

        // Construir la URL con los parámetros
        String urlString = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=" 
                           + startDate + "&endtime=" + endDate;

        try {
            // Crear un objeto URL
            URL url = new URL(urlString);
            
            // Abrir la conexión
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            // Leer la respuesta
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Cerrar las conexiones
            in.close();
            conn.disconnect();

            // Procesar la respuesta JSON
            JSONObject jsonResponse = new JSONObject(content.toString());
            JSONArray features = jsonResponse.getJSONArray("features");

            // Mostrar la respuesta JSON de manera ordenada
            System.out.println("Resultados de terremotos entre " + startDate + " y " + endDate + ":");
            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject properties = feature.getJSONObject("properties");
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");
                
                // Extraer y mostrar información relevante
                String place = properties.getString("place");
                double magnitude = properties.getDouble("mag");
                long time = properties.getLong("time");
                String type = properties.getString("type");
                String urlInfo = properties.getString("url");
                double depth = coordinates.getDouble(2);
                
                // Convertir latitud y longitud para obtener la zona horaria local
                double longitude = coordinates.getDouble(0);
                double latitude = coordinates.getDouble(1);

                // Obtener la zona horaria local
                String timeZone = getTimeZone(latitude, longitude);
                
                // Convertir el tiempo de Unix a formato legible en la zona horaria local
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.of(timeZone));
                String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"));

                System.out.println("Lugar: " + place);
                System.out.println("Magnitud: " + magnitude);
                System.out.println("Fecha y hora local: " + formattedDate + " (" + timeZone + ")");
                System.out.println("Profundidad: " + depth + " km");
                System.out.println("Tipo: " + type);
                System.out.println("Más información: " + urlInfo);
                System.out.println("---------------------------");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    // Método para obtener la zona horaria basada en la latitud y longitud
    private static String getTimeZone(double lat, double lon) {
        String[] availableZoneIds = TimeZone.getAvailableIDs();
        for (String id : availableZoneIds) {
            ZoneId zoneId = ZoneId.of(id);
            if (zoneId.getRules().getOffset(Instant.now()).getTotalSeconds() == 
                TimeZone.getTimeZone(zoneId).getRawOffset() / 1000) {
                return zoneId.toString();
            }
        }
        return "UTC";
    }
}

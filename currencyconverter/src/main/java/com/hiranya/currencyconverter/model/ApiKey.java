import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "api_keys")
public class ApiKey {

    @Id
    private String id;

    private String keyValue;
    private String clientName;
    private boolean active;
}
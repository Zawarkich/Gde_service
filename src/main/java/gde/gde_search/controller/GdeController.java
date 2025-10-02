package gde.gde_search.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GdeController {

    @GetMapping("/")
    public String root() {
        return "<html><head><meta charset=\"utf-8\"></head><body>"
                + "<h1>список группы</h1>"
                + "<table border=\"1\" cellpadding=\"6\">"
                + "<thead><tr><th>ID</th><th>ФИО</th><th>Взвод</th><th>группа</th><th>местонахождение</th></tr></thead>"
                + "<tbody>"
                + "<tr><td>1</td><td>Головатенко</td><td>4</td><td>334701</td><td>-</td></tr>"
                + "<tr><td>2</td><td>Козлов</td><td>4</td><td>334701</td><td>-</td></tr>"
                + "<tr><td>3</td><td>Королёв</td><td>4</td><td>334701</td><td>-</td></tr>"
                + "<tr><td>4</td><td>Ленько</td><td>4</td><td>334701</td><td>-</td></tr>"
                + "<tr><td>5</td><td>Сантоцкий</td><td>4</td><td>334701</td><td>-</td></tr>"
                + "</tbody></table></body></html>";
    }

}

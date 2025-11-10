package gde.gde_search.controller;

import gde.gde_search.model.GroupMember;
import gde.gde_search.service.GdeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GdeController {

    private final GdeService service;

    public GdeController(GdeService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        GroupMember user = (GroupMember) session.getAttribute("user");
        return pageHead(user)
                + "<div class=\"page\">"
                + authBar(user)
                + "<h1>Система РАСХОД</h1>"
                + "<div class=\"controls\">"
                + "<div class=\"controls-row\">"
                + "<a href=\"/vzv1\"><button class=\"btn btn-large\">1</button></a>"
                + "<a href=\"/vzv2\"><button class=\"btn btn-large\">2</button></a>"
                + "<a href=\"/vzv3\"><button class=\"btn btn-large\">3</button></a>"
                + "<a href=\"/vzv4\"><button class=\"btn btn-large\">4</button></a>"
                + "<a href=\"/all\"><button class=\"btn btn-large\">общий список</button></a>"
                + "</div>"
                + (user != null ? "<div class=\"controls-row\"><a href=\"/change-location\"><button class=\"btn btn-large btn-location\">изменить местонахождение</button></a></div>" : "")
                + "</div>"
                + "</div></body></html>";
    }

    @GetMapping("/vzv1")
    public String vzv1(HttpSession session) {
        GroupMember user = (GroupMember) session.getAttribute("user");
        List<GroupMember> members = service.getByVzvod(1);
        return renderTableWithBack(members, "Список взвода 1", user);
    }

    @GetMapping("/vzv2")
    public String vzv2(HttpSession session) {
        GroupMember user = (GroupMember) session.getAttribute("user");
        List<GroupMember> members = service.getByVzvod(2);
        return renderTableWithBack(members, "Список взвода 2", user);
    }

    @GetMapping("/vzv3")
    public String vzv3(HttpSession session) {
        GroupMember user = (GroupMember) session.getAttribute("user");
        List<GroupMember> members = service.getByVzvod(3);
        return renderTableWithBack(members, "Список взвода 3", user);
    }

    @GetMapping("/vzv4")
    public String vzv4(HttpSession session) {
        GroupMember user = (GroupMember) session.getAttribute("user");
        List<GroupMember> members = service.getByVzvod(4);
        return renderTableWithBack(members, "Список взвода 4", user);
    }

    @GetMapping("/all")
    public String all(HttpSession session) {
        GroupMember user = (GroupMember) session.getAttribute("user");
        List<GroupMember> members = service.getAll();
        return renderTableWithBack(members, "Общий список", user);
    }

    @GetMapping("/login-page")
    public String loginPage(HttpSession session) {
        GroupMember user = (GroupMember) session.getAttribute("user");
        return pageHeadWithBack(user)
            + "<div class=\"page\">"
            +   "<div class=\"login-container\">"
            +     "<h1>Login</h1>"
            +     "<form class=\"login-form\" method=\"post\" action=\"/login\">"
            +       "<div class=\"form-group\">"
            +         "<input name=\"login\" type=\"text\" placeholder=\"Логин\" class=\"form-input\" required>"
            +       "</div>"
            +       "<div class=\"form-group\">"
            +         "<input name=\"password\" type=\"password\" placeholder=\"Пароль\" class=\"form-input\" required>"
            +       "</div>"
            +       "<button type=\"submit\" class=\"btn btn-large\">Вход</button>"
            +     "</form>"
            +     "<div class=\"help-link\"><a href=\"#\" class=\"small-link\">где взять пароль?</a></div>"
            +   "</div>"
            + "</div></body></html>";
    }

    @PostMapping("/login")
    public String login(@RequestParam String login, @RequestParam String password,
                        HttpSession session) {
        try {
            GroupMember authenticated = service.authenticateByLoginAndPassword(login, password);
            if (authenticated == null) {
                return pageHeadWithBack(null) + "<div class=\"page\"><div class=\"login-container\"><div class=\"subtitle\">Неверные данные</div><a href=\"/login-page\"><button class=\"btn\">Назад</button></a></div></div></body></html>";
            }
            session.setAttribute("user", authenticated);
            return pageHead(authenticated)
                    + "<div class=\"page\">" + authBar(authenticated)
                    + "<h2>Вход выполнен</h2><a href=\"/\"><button class=\"btn\">На главную</button></a>"
                    + "</div></body></html>";
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return pageHeadWithBack(null) + "<div class=\"page\"><div class=\"login-container\"><div class=\"subtitle\">Ошибка при авторизации. Повторите попытку.</div><a href=\"/login-page\"><button class=\"btn\">Назад</button></a></div></div></body></html>";
        }
    }

    @GetMapping("/change-location")
    public String changeLocationPage(HttpSession session) {
        GroupMember user = (GroupMember) session.getAttribute("user");
        if (user == null) {
            return pageHeadWithBack(null) + "<div class=\"page\"><h2>Необходима авторизация</h2><a href=\"/login-page\"><button class=\"btn\">Войти</button></a></div></body></html>";
        }
        return pageHeadWithBack(user)
                + "<div class=\"page\">"
                + authBar(user)
                + "<h1>Изменить местонахождение</h1>"
                + "<div class=\"quick-buttons-section\">"
                + "<button type=\"button\" class=\"quick-btn-large\" onclick=\"setLocation('На месте')\">На месте</button>"
                + "<button type=\"button\" class=\"quick-btn-large\" onclick=\"setLocation('Болен')\">Болен</button>"
                + "<button type=\"button\" class=\"quick-btn-large\" onclick=\"setLocation('Наряд')\">Наряд</button>"
                + "<button type=\"button\" class=\"quick-btn-large\" onclick=\"setLocation('Командировка')\">Командировка</button>"
                + "</div>"
                + "<div class=\"login-container\">"
                + "<form class=\"login-form\" method=\"post\" action=\"/update-location\">"
                + "<div class=\"form-group\">"
                + "<label>Текущее местонахождение: " + user.location() + "</label>"
                + "</div>"
                + "<div class=\"form-group\">"
                + "<input name=\"newLocation\" type=\"text\" placeholder=\"Новое местонахождение\" class=\"form-input\" id=\"locationInput\" required>"
                + "</div>"
                + "<button type=\"submit\" class=\"btn btn-large\">Обновить</button>"
                + "</form>"
                + "</div>"
                + "<script>"
                + "function setLocation(location) {"
                + "  document.getElementById('locationInput').value = location;"
                + "}"
                + "</script>"
                + "</div></body></html>";
    }

    @PostMapping("/update-location")
    public String updateLocation(@RequestParam String newLocation, HttpSession session) {
        GroupMember user = (GroupMember) session.getAttribute("user");
        if (user == null) {
            return pageHeadWithBack(null) + "<div class=\"page\"><h2>Необходима авторизация</h2><a href=\"/login-page\"><button class=\"btn\">Войти</button></a></div></body></html>";
        }
        
        try {
            service.updateLocation(user.id(), newLocation);
            // Обновляем пользователя в сессии
            GroupMember updatedUser = service.getById(user.id());
            session.setAttribute("user", updatedUser);
            
            return pageHeadWithBack(updatedUser)
                    + "<div class=\"page\">"
                    + authBar(updatedUser)
                    + "<h2>Местонахождение успешно обновлено</h2>"
                    + "<p>Новое местонахождение: " + newLocation + "</p>"
                    + "<a href=\"/\"><button class=\"btn\">На главную</button></a>"
                    + "</div></body></html>";
        } catch (Exception e) {
            return pageHeadWithBack(user)
                    + "<div class=\"page\">"
                    + authBar(user)
                    + "<h2>Ошибка при обновлении</h2>"
                    + "<p>Не удалось обновить местонахождение. Попробуйте еще раз.</p>"
                    + "<a href=\"/change-location\"><button class=\"btn\">Попробовать снова</button></a>"
                    + "</div></body></html>";
        }
    }

    @PostMapping("/change-vzvod-location")
    public String changeVzvodLocation(@RequestParam int vzvod, @RequestParam String newLocation, HttpSession session) {
        GroupMember user = (GroupMember) session.getAttribute("user");
        if (user == null) {
            return pageHeadWithBack(null) + "<div class=\"page\"><h2>Необходима авторизация</h2><a href=\"/login-page\"><button class=\"btn\">Войти</button></a></div></body></html>";
        }
        
        // Проверяем, что у пользователя есть права (статус 2 или 3)
        if (user.presence() == null || (user.presence() != 2 && user.presence() != 3)) {
            return pageHeadWithBack(user) + "<div class=\"page\"><h2>Недостаточно прав</h2><p>Только пользователи со статусом 2 или 3 могут изменять местоположение взводов.</p><a href=\"/\"><button class=\"btn\">На главную</button></a></div></body></html>";
        }
        
        try {
            service.updateVzvodLocation(vzvod, newLocation.trim());
            
            return pageHeadWithBack(user)
                    + "<div class=\"page\">"
                    + authBar(user)
                    + "<h2>Местоположение взвода успешно обновлено</h2>"
                    + "<p>Взвод " + vzvod + " теперь находится: " + newLocation + "</p>"
                    + "<a href=\"/\"><button class=\"btn\">На главную</button></a>"
                    + "</div></body></html>";
        } catch (Exception e) {
            return pageHeadWithBack(user)
                    + "<div class=\"page\">"
                    + authBar(user)
                    + "<h2>Ошибка при обновлении</h2>"
                    + "<p>Не удалось обновить местоположение взвода. Попробуйте еще раз.</p>"
                    + "<a href=\"/\"><button class=\"btn\">На главную</button></a>"
                    + "</div></body></html>";
        }
    }

    @PostMapping("/change-soldier-location")
    public String changeSoldierLocation(@RequestParam int soldierId, @RequestParam String newLocation, HttpSession session) {
        GroupMember user = (GroupMember) session.getAttribute("user");
        if (user == null) {
            return pageHeadWithBack(null) + "<div class=\"page\"><h2>Необходима авторизация</h2><a href=\"/login-page\"><button class=\"btn\">Войти</button></a></div></body></html>";
        }
        
        // Проверяем, что у пользователя есть права (статус 2 или 3)
        if (user.presence() == null || (user.presence() != 2 && user.presence() != 3)) {
            return pageHeadWithBack(user) + "<div class=\"page\"><h2>Недостаточно прав</h2><p>Только пользователи со статусом 2 или 3 могут изменять местоположение солдат.</p><a href=\"/\"><button class=\"btn\">На главную</button></a></div></body></html>";
        }
        
        try {
            service.updateLocation(soldierId, newLocation.trim());
            
            // Обновляем пользователя в сессии, если это он сам
            GroupMember updatedUser = null;
            if (user.id() == soldierId) {
                updatedUser = service.getById(soldierId);
                session.setAttribute("user", updatedUser);
            } else {
                updatedUser = user;
            }
            
            return pageHeadWithBack(updatedUser)
                    + "<div class=\"page\">"
                    + authBar(updatedUser)
                    + "<h2>Местоположение солдата успешно обновлено</h2>"
                    + "<p>Новое местонахождение: " + newLocation + "</p>"
                    + "<a href=\"/\"><button class=\"btn\">На главную</button></a>"
                    + "</div></body></html>";
        } catch (Exception e) {
            return pageHeadWithBack(user)
                    + "<div class=\"page\">"
                    + authBar(user)
                    + "<h2>Ошибка при обновлении</h2>"
                    + "<p>Не удалось обновить местоположение солдата. Попробуйте еще раз.</p>"
                    + "<a href=\"/\"><button class=\"btn\">На главную</button></a>"
                    + "</div></body></html>";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return pageHead(null) + "<div class=\"page\"><h2>Вы вышли</h2><a href=\"/\"><button class=\"btn\">На главную</button></a></div></body></html>";
    }

    private String renderTableWithBack(List<GroupMember> members, String title, GroupMember user) {
        StringBuilder html = new StringBuilder();
        html.append(pageHeadWithBack(user));
        html.append("<div class=\"page\">");
        html.append(authBar(user));
        html.append("<h1>").append(title).append("</h1>");
        html.append("<table>");
        html.append("<thead><tr><th>ID</th><th>ФИО</th><th>Взвод</th><th>Группа</th><th>Местонахождение</th><th>Управление</th></tr></thead>");
        html.append("<tbody>");
        for (GroupMember m : members) {
            html.append("<tr>")
                .append("<td>").append(m.id()).append("</td>");
            
            // Выделяем ФИО жирным шрифтом, если presence = 3
            String fioStyle = (m.presence() != null && m.presence() == 3) ? "font-weight:bold;" : "";
            html.append("<td><a href=\"").append(m.tg()).append("\" target=\"_blank\" rel=\"noopener noreferrer\" style=\"color:inherit;text-decoration:none;").append(fioStyle).append("\">")
                .append(m.fio()).append("</a></td>")
                .append("<td>").append(m.vzvod()).append("</td>")
                .append("<td>").append(m.group()).append("</td>")
                .append("<td>").append(m.location()).append("</td>");
            
            // Добавляем столбец с кнопками управления (только для статусов 2 и 3)
            html.append("<td>");
            if (user != null && (user.presence() != null && (user.presence() == 2 || user.presence() == 3))) {
                html.append("<button onclick=\"changeSoldierLocation(").append(m.id()).append(", '").append(m.fio()).append("')\" class=\"btn btn-small\">Изменить местоположение</button>");
            } else {
                html.append("-");
            }
            html.append("</td>")
                .append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("<div class=\"back-section\"><a href=\"/\" class=\"back-link\">← Назад к главной</a></div>");
        
        // Добавляем JavaScript для изменения местоположения отдельного солдата
        html.append("<script>")
            .append("function changeSoldierLocation(id, fio) {")
            .append("  var newLocation = prompt('Введите новое местоположение для ' + fio + ':');")
            .append("  if (newLocation && newLocation.trim() !== '') {")
            .append("    if (confirm('Вы уверены, что хотите изменить местоположение ' + fio + ' на \"' + newLocation + '\"?')) {")
            .append("      var form = document.createElement('form');")
            .append("      form.method = 'POST';")
            .append("      form.action = '/change-soldier-location';")
            .append("      var idInput = document.createElement('input');")
            .append("      idInput.type = 'hidden';")
            .append("      idInput.name = 'soldierId';")
            .append("      idInput.value = id;")
            .append("      var locationInput = document.createElement('input');")
            .append("      locationInput.type = 'hidden';")
            .append("      locationInput.name = 'newLocation';")
            .append("      locationInput.value = newLocation;")
            .append("      form.appendChild(idInput);")
            .append("      form.appendChild(locationInput);")
            .append("      document.body.appendChild(form);")
            .append("      form.submit();")
            .append("    }")
            .append("  }")
            .append("}")
            .append("</script>");
        
        html.append("</div></body></html>");
        return html.toString();
    }

    private String pageHead(GroupMember user){
        return "<html><head><meta charset=\"utf-8\"><link rel=\"stylesheet\" href=\"/styles.css\"></head><body>";
    }

    private String pageHeadWithBack(GroupMember user){
        return "<html><head><meta charset=\"utf-8\"><link rel=\"stylesheet\" href=\"/styles.css\"></head><body>";
    }


    private String authBar(GroupMember user){
        if (user == null) {
            return "<div class=\"auth\"><a href=\"/login-page\"><button class=\"btn btn-link\">Авторизация</button></a></div>";
        }
        return "<div class=\"auth\"><div class=\"subtitle\">" + user.fio() + ", взвод " + user.vzvod() + ", группа " + user.group() + "<br/>" + user.location() + "</div>"
                + "<form method=\"post\" action=\"/logout\" style=\"display:inline;margin-left:8px\"><button class=\"btn btn-link\">Выйти</button></form></div>";
    }

}

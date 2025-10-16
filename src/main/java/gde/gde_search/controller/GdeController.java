package gde.gde_search.controller;

import gde.gde_search.model.GroupMember;
import gde.gde_search.service.GdeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GdeController {

    private final GdeService service;

    public GdeController(GdeService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() {
        return pageHead()
                + "<div class=\"page\">"
                + "<div class=\"auth\"><a href=\"/login-page\"><button class=\"btn btn-link\">Авторизация</button></a></div>"
                + "<h1>Система РАСХОД</h1>"
                + "<div class=\"controls\">"
                + "<button class=\"btn btn-large\" disabled>1</button>"//пропишу полный спиок\подключу бд и разблокирую
                + "<button class=\"btn btn-large\" disabled>2</button>"//и тут
                + "<button class=\"btn btn-large\" disabled>3</button>"//и здесь не забыть
                + "<a href=\"/vzv4\"><button class=\"btn btn-large\">4</button></a>"
                + "<a href=\"/all\"><button class=\"btn btn-large\">общий список</button></a>"
                + "</div>"
                + "</div></body></html>";
    }

    @GetMapping("/vzv1")
    public String vzv1() { return basicPage("VZV1 - пока пусто"); }

    @GetMapping("/vzv2")
    public String vzv2() { return basicPage("VZV2 - пока пусто"); }

    @GetMapping("/vzv3")
    public String vzv3() { return basicPage("VZV3 - пока пусто"); }

    @GetMapping("/vzv4")
    public String vzv4() {
        List<GroupMember> members = service.getAll();
        return renderTableWithBack(members, "Список взвода 4");
    }

    @GetMapping("/all")
    public String all() {
        List<GroupMember> members = service.getAll();
        return renderTableWithBack(members, "Общий список");
    }

    @GetMapping("/login-page")
    public String loginPage() {
        return pageHeadWithBack()
            + "<div class=\"page\">"
            +   "<div class=\"login-container\">"
            +     "<h1>Login</h1>"
            +     "<form class=\"login-form\">"
            +       "<div class=\"form-group\">"
            +         "<input type=\"text\" placeholder=\"ФИО\" class=\"form-input\" required>"
            +       "</div>"
            +       "<div class=\"form-group\">"
            +         "<input type=\"password\" placeholder=\"Пароль\" class=\"form-input\" required>"
            +       "</div>"
            +       "<button type=\"submit\" class=\"btn btn-large\">Вход</button>"
            +     "</form>"
            +     "<div class=\"help-link\"><a href=\"#\" class=\"small-link\">где взять пароль?</a></div>"
            +   "</div>"
            + "</div></body></html>";
    }

   
    private String renderTableWithBack(List<GroupMember> members, String title) {
        StringBuilder html = new StringBuilder();
        html.append(pageHeadWithBack());
        html.append("<div class=\"page\">");
        html.append("<h1>").append(title).append("</h1>");
        html.append("<table>");
        html.append("<thead><tr><th>ID</th><th>ФИО</th><th>Взвод</th><th>Группа</th><th>Местонахождение</th></tr></thead>");
        html.append("<tbody>");
        for (GroupMember m : members) {
            html.append("<tr>")
                .append("<td>").append(m.id()).append("</td>")
                .append("<td><a href=\"").append(m.tg()).append("\" target=\"_blank\" rel=\"noopener noreferrer\" style=\"color:inherit;text-decoration:none\">")
                .append(m.fio()).append("</a></td>")
                .append("<td>").append(m.vzvod()).append("</td>")
                .append("<td>").append(m.group()).append("</td>")
                .append("<td>").append(m.location()).append("</td>")
                .append("</tr>");
        }
        html.append("</tbody></table></div></body></html>");
        return html.toString();
    }

    private String pageHead(){
        return "<html><head><meta charset=\"utf-8\"><link rel=\"stylesheet\" href=\"/styles.css\"></head><body>";
    }

    private String pageHeadWithBack(){
        return "<html><head><meta charset=\"utf-8\"><link rel=\"stylesheet\" href=\"/styles.css\"></head><body><div class=\"back-button\"><a href=\"/\"><button class=\"btn btn-back\">←</button></a></div>";
    }

    private String basicPage(String text){
        return pageHeadWithBack() + "<div class=\"page\"><h2>"+text+"</h2></div></body></html>";
    }

}

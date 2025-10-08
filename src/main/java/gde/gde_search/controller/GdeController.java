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
        return "<html><head><meta charset=\"utf-8\"></head><body>"
                + "<h1>Система РАСХОД</h1>"
                + "<div style=\"margin-bottom:16px\">"
                + "<button disabled style=\"margin-right:6px\">1</button>"
                + "<button disabled style=\"margin-right:6px\">2</button>"
                + "<button disabled style=\"margin-right:6px\">3</button>"
                + "<a href=\"/vzv4\"><button style=\"margin-right:6px\">4</button></a>"
                + "<a href=\"/all\"><button style=\"margin-right:6px\">общий список</button></a>"
                + "<a href=\"/login-page\" style=\"float:right\"><button>Авторизация</button></a>"
                + "</div>"
                + "</body></html>";
    }

    @GetMapping("/vzv1")
    public String vzv1() { return "<html><body><h2>VZV1 - пока пусто</h2></body></html>"; }

    @GetMapping("/vzv2")
    public String vzv2() { return "<html><body><h2>VZV2 - пока пусто</h2></body></html>"; }

    @GetMapping("/vzv3")
    public String vzv3() { return "<html><body><h2>VZV3 - пока пусто</h2></body></html>"; }

    @GetMapping("/vzv4")
    public String vzv4() {
        List<GroupMember> members = service.getAll();
        return renderTable(members, "список взвода 4");
    }

    @GetMapping("/all")
    public String all() {
        List<GroupMember> members = service.getAll();
        return renderTable(members, "общий список");
    }

    @GetMapping("/login-page")
    public String loginPage() {
        return "<html><body><h2>Авторизация / Регистрация (не работает)</h2></body></html>";
    }

    private String renderTable(List<GroupMember> members, String title) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset=\"utf-8\"></head><body>");
        html.append("<h1>").append(title).append("</h1>");
        html.append("<table border=\"1\" cellpadding=\"6\">");
        html.append("<thead><tr><th>ID</th><th>ФИО</th><th>Взвод</th><th>группа</th><th>местонахождение</th></tr></thead>");
        html.append("<tbody>");
        for (GroupMember m : members) {
            html.append("<tr>")
                .append("<td>").append(m.id()).append("</td>")
                .append("<td>").append(m.fio()).append("</td>")
                .append("<td>").append(m.vzvod()).append("</td>")
                .append("<td>").append(m.group()).append("</td>")
                .append("<td>").append(m.location()).append("</td>")
                .append("</tr>");
        }
        html.append("</tbody></table></body></html>");
        return html.toString();
    }

}

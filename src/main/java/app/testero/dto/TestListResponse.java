package app.testero.dto;

import java.util.List;

public record TestListResponse(List<TestListItem> tests) {

    public record TestListItem(
            String id,
            String title,
            String date,
            int timerMinutes,
            int questionsPerTest
    ) {}
}

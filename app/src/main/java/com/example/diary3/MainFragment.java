package com.example.diary3;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.diary3.data.entity.Diary;
import com.example.diary3.data.AppDatabase;
import com.example.diary3.data.dao.DiaryDao;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.json.JSONObject;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


public class MainFragment extends Fragment {

    private MaterialCalendarView materialCalendarView;

    private EventDecorator eventDecorator;

    private static final int REQUEST_CODE_DIARY_WRITE = 100;

    private DiaryDao diaryDao;

    private ImageView calendarCharacterImageView;

    private TextView monthlyFeedbackTextView;


    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);



        materialCalendarView = view.findViewById(R.id.calendarView);

        materialCalendarView.state().edit()
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        materialCalendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)));
        materialCalendarView.setTitleFormatter(new KoreanTitleFormatter());
        materialCalendarView.setHeaderTextAppearance(R.style.CalendarWidgetHeader);
        materialCalendarView.addDecorator(new SundayDecorator());

        monthlyFeedbackTextView = view.findViewById(R.id.monthlyFeedbackTextView); // 레이아웃의 TextView ID

        diaryDao = AppDatabase.getInstance(requireContext()).diaryDao();

        new LoadDiaryDatesTask().execute();

        materialCalendarView.setOnDateChangedListener((widget, date, selected) -> {
            Intent intent = new Intent(getActivity(), DiaryWriteActivity.class);
            intent.putExtra("selectedDate", date.getDate().toString());
            startActivityForResult(intent, REQUEST_CODE_DIARY_WRITE);
        });

        calendarCharacterImageView = view.findViewById(R.id.selected_character);

        calendarCharacterImageView.setOnClickListener(v -> {
            CalendarDay currentMonth = materialCalendarView.getCurrentDate();
            int year = currentMonth.getYear();
            int month = currentMonth.getMonth(); // 1-based month

            new GetMonthlyFeedbackTask().execute(year, month);
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateCharacterImage();
    }


    private int getCharacterResId(String characterKey) {
        switch (characterKey) {
            case "character1": return R.drawable.character1;
            case "character2": return R.drawable.character2;
            case "character3": return R.drawable.character3;
            default: return R.drawable.character1;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DIARY_WRITE && resultCode == getActivity().RESULT_OK) {
            String updatedDate = data.getStringExtra("updatedDate");
            Log.d("MainFragment", "onActivityResult - updatedDate: " + updatedDate);
            if (updatedDate != null) {
                updateSingleDate(updatedDate);
            }
        }
    }


    private void updateSingleDate(String dateStr) {
        if (eventDecorator == null) {
            eventDecorator = new EventDecorator(Color.RED, new HashSet<>());
            materialCalendarView.addDecorator(eventDecorator);
        }

        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                String date = params[0];
                Diary diary = diaryDao.getDiaryByDate(date);
                return diary != null;
            }

            @Override
            protected void onPostExecute(Boolean exists) {
                try {
                    LocalDate localDate = LocalDate.parse(dateStr);
                    CalendarDay day = CalendarDay.from(localDate);

                    if (exists) {
                        eventDecorator.addDate(day);
                        Log.d("MainFragment", "updateSingleDate - 날짜 추가: " + dateStr);
                    } else {
                        eventDecorator.removeDate(day);
                        Log.d("MainFragment", "updateSingleDate - 날짜 제거: " + dateStr);
                    }
                    materialCalendarView.invalidateDecorators();
                } catch (Exception e) {
                    Log.e("updateSingleDate", "잘못된 날짜 형식: " + dateStr);
                }
            }
        }.execute(dateStr);
    }

    private class LoadDiaryDatesTask extends AsyncTask<Void, Void, HashSet<CalendarDay>> {
        @Override
        protected HashSet<CalendarDay> doInBackground(Void... voids) {
            HashSet<CalendarDay> eventDates = new HashSet<>();
            List<Diary> diaryList = diaryDao.getAllDiaries();

            for (Diary diary : diaryList) {
                try {
                    String dateStr = diary.getDate(); // getDate()는 Diary 클래스의 날짜 getter
                    LocalDate localDate = LocalDate.parse(dateStr);
                    eventDates.add(CalendarDay.from(localDate));
                } catch (Exception e) {
                    Log.e("LoadDiaryDatesTask", "날짜 파싱 실패: " + diary.toString());
                }
            }
            return eventDates;
        }

        @Override
        protected void onPostExecute(HashSet<CalendarDay> eventDates) {
            eventDecorator = new EventDecorator(Color.RED, eventDates);
            materialCalendarView.addDecorator(eventDecorator);
            materialCalendarView.invalidateDecorators();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCharacterImage();
    }

    private void updateCharacterImage() {
        if (calendarCharacterImageView == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String selectedCharacter = prefs.getString("selectedCharacter", "character1");
        int resId = getCharacterResId(selectedCharacter);
        calendarCharacterImageView.setImageResource(resId);
    }


    private class GetMonthlyFeedbackTask extends AsyncTask<Integer, Void, String> {
        private int currentYear;
        private int currentMonth;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            monthlyFeedbackTextView.setText("월간 피드백을 생성 중..."); // 로딩 메시지 표시
        }
        // ----------------------------------------


        @Override
        protected String doInBackground(Integer... params) {
            currentYear = params[0];
            currentMonth = params[1]; // 1-based month

            LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());


            List<Diary> allDiaries = diaryDao.getAllDiaries();

            List<String> feedbacks = allDiaries.stream()
                    .filter(diary -> {
                        try {
                            LocalDate diaryDate = LocalDate.parse(diary.getDate());
                            return diaryDate.getYear() == currentYear && diaryDate.getMonthValue() == currentMonth;
                        } catch (Exception e) {
                            Log.e("GetMonthlyFeedbackTask", "날짜 파싱 오류: " + diary.getDate());
                            return false;
                        }
                    })
                    .map(Diary::getFeedback) // Diary 엔티티에 getFeedback() 메서드가 있어야 합니다.
                    .filter(feedback -> feedback != null && !feedback.isEmpty()) // null 또는 빈 문자열 피드백 제외
                    .collect(Collectors.toList());

            if (feedbacks.isEmpty()) {
                return "이번 달에는 피드백을 분석할 일기가 없습니다.";
            }

            String combinedFeedbacks = String.join("\n", feedbacks);
            Log.d("MonthlyFeedback", "Combined Feedbacks: " + combinedFeedbacks);

            try {
                JSONObject requestJson = new JSONObject();
                requestJson.put("combined_text", combinedFeedbacks); // AI 요청에 맞는 키 사용

                requestJson.put("year_month", String.format("%04d-%02d", currentYear, currentMonth));

                JSONObject apiResult = EmotionRequestHelper.callSyncMonthlySummaryApi(requestJson);

                if (apiResult != null && apiResult.has("monthly_summary")) { // AI 응답에 맞는 키 사용
                    return apiResult.getString("monthly_summary");
                } else {
                    return "월간 피드백을 가져오지 못했습니다. (API 응답 오류)";
                }
            } catch (Exception e) {
                Log.e("GetMonthlyFeedbackTask", "월간 피드백 API 호출 실패: " + e.getMessage());
                return "월간 피드백 생성 중 오류가 발생했습니다: " + e.getLocalizedMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            monthlyFeedbackTextView.setText(result); // 결과를 TextView에 표시
        }
    }

    private static class SundayDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            LocalDate date = day.getDate();
            return date.getDayOfWeek() == DayOfWeek.SUNDAY;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(Color.RED));
        }

    }

    private static class KoreanTitleFormatter implements TitleFormatter {
        @Override
        public CharSequence format(CalendarDay day) {
            int year = day.getYear();
            int month = day.getMonth(); // 1~12
            return year + "년 " + month + "월";
        }
    }


    private static class EventDecorator implements DayViewDecorator {
        private final int color;
        private final HashSet<CalendarDay> dates;

        public EventDecorator(int color, Collection<CalendarDay> dates) {
            this.color = color;
            this.dates = new HashSet<>(dates);
        }

        public void addDate(CalendarDay day) {
            dates.add(day);
        }

        public void removeDate(CalendarDay day) {
            dates.remove(day);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(8, color)); // 점 표시
        }
    }

    }

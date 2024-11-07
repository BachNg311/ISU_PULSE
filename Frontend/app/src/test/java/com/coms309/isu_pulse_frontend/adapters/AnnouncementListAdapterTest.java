package com.coms309.isu_pulse_frontend.adapters;

import static androidx.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertEquals;

import com.coms309.isu_pulse_frontend.loginsignup.UserSession;
import com.coms309.isu_pulse_frontend.model.Announcement;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementListAdapterTest {

    @Test
    public void testAnnouncementAdapter() {
        // Create a list of announcements with all necessary fields
        List<Announcement> announcements = new ArrayList<>();
        announcements.add(new Announcement(101L, "Test Announcement", 1L, "kopper.cs", "2024-11-06T10:00:00", "Com S 227"));
        String userRole = UserSession.getInstance(getContext()).getUserType();
        // Initialize the adapter with the list of announcements
        AnnouncementListAdapter adapter = new AnnouncementListAdapter(announcements, "TEACHER".equals(userRole));

        // Check if the adapter has the correct item count
        assertEquals(1, adapter.getItemCount());
    }
}
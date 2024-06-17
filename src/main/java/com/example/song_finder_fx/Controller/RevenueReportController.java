package com.example.song_finder_fx.Controller;

import com.example.song_finder_fx.DatabasePostgres;
import com.example.song_finder_fx.Model.ArtistReport;
import com.example.song_finder_fx.Model.CoWriterSummary;
import com.example.song_finder_fx.Model.RevenueReport;
import com.example.song_finder_fx.Model.Songs;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public record RevenueReportController(ArtistReport report) {

    public ArtistReport calculateRevenue() throws SQLException {
        // Refreshing Tables
        int status = DatabasePostgres.refreshSummaryTable(report);

        if (status > 0) {
            System.out.println("Summary Table Refreshed\n");
        } else {
            System.out.println("Error Updating Summary Report\n");
        }

        // Getting gross revenue and partner share
        RevenueReport grossNPartnerShare = DatabasePostgres.getPayeeGrossRevNew(report);
        Double grossRevenue = grossNPartnerShare.getReportedRoyalty();
        Double partnerShare = grossNPartnerShare.getAfterDeductionRoyalty();
        report.setGrossRevenue(grossRevenue);
        report.setPartnerShare(partnerShare);

        // Getting top 5 most performing songs
        ArrayList<Songs> topP_Songs = DatabasePostgres.getTopPerformingSongs(report.getArtist().getName()); // This object only contains ISRC and Revenue for now. Need to get Song Name
        report.setTopPerformingSongs(topP_Songs);

        // Getting report month
        /*String dateString = DatabasePostgres.getSalesDate();
        String[] date = dateString.split("-");
        String month = date[1];
        System.out.println("month = " + month);
        report.setMonth(month);*/

        // Getting Co-Writer Payments
        report.setCoWritterList(DatabasePostgres.getCoWriterPayments(report.getArtist().getName()));
        List<CoWriterSummary> coWriterSummaryList = DatabasePostgres.getCoWriterPaymentSummary(report.getArtist().getName());
        report.setCoWriterPaymentSummary(coWriterSummaryList);

        return report;
    }
}

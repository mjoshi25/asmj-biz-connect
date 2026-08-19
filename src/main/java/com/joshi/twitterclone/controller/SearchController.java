package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.dto.AutocompleteResultDto;
import com.joshi.twitterclone.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search/autocomplete")
    public String autocomplete(@RequestParam(name = "q", required = false, defaultValue = "") String query,
                               Model model) {
        AutocompleteResultDto results = searchService.getAutocompleteSuggestions(query);
        model.addAttribute("results", results);
        return "fragments/search-autocomplete :: dropdown";
    }
}
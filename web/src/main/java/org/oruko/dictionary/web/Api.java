package org.oruko.dictionary.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.oruko.dictionary.model.Name;
import org.oruko.dictionary.model.NameEntry;
import org.oruko.dictionary.model.NameEntryRepository;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;

/**
 * End point for inserting and retrieving Name Entries
 * This would be the end point the clients would interact with to get names in and out of the dictionary
 * TODO Consider moving this as a stand alone service
 * Created by dadepo on 2/12/15.
 */
@Controller
public class Api {

    Logger logger = LoggerFactory.getLogger(Api.class);

    @Autowired
    NameEntryRepository nameEntryRepository;

    @RequestMapping(value = "/v1/name", method = RequestMethod.POST)
    public ResponseEntity<String> addName(@Valid NameEntry entry, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            entry.setName(entry.getName().toLowerCase());
            nameEntryRepository.save(entry);
            return new ResponseEntity<String>("success", HttpStatus.CREATED);
        }
        return new ResponseEntity<String>(formatErrorMessage(bindingResult), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(value = "/v1/names", method = RequestMethod.GET)
    @ResponseBody
    public String getAllNames() throws JsonProcessingException {
        List<Name> names = new ArrayList<>();
        Iterable<NameEntry> allNameEntries = nameEntryRepository.findAll();

        allNameEntries.forEach(nameEntry -> {
            names.add(nameEntry.toName());
        });

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(names);
    }

    @RequestMapping(value = "/v1/names/{name}", method = RequestMethod.GET)
    @ResponseBody
    public String getName(@PathVariable String name) throws JsonProcessingException {
        NameEntry nameEntry = nameEntryRepository.findByName(name);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(nameEntry.toName());
    }


    //=====================================Helpers=========================================================//

    private String formatErrorMessage(BindingResult bindingResult) {
        StringBuilder builder = new StringBuilder();
        for (FieldError error : bindingResult.getFieldErrors()) {
            builder.append(error.getField() + " " + error.getDefaultMessage());
        }
        return builder.toString();
    }
}
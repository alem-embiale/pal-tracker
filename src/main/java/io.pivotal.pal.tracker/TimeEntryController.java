package io.pivotal.pal.tracker;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/time-entries")
public class TimeEntryController {

    private TimeEntryRepository timeEntryRepository;
    private DistributionSummary timeEntrySummary;
    private Counter actionCounter;


    public TimeEntryController(TimeEntryRepository timeEntryRepository,
                               MeterRegistry meterRegistry) {
        this.timeEntryRepository = timeEntryRepository;
        this.timeEntrySummary = meterRegistry.summary("timeEntry.summary");
        this.actionCounter = meterRegistry.counter("timeEntry.actionCounter");
    }

    @PostMapping
    public ResponseEntity create(@RequestBody TimeEntry timeEntryToCreate) {
        try {
            TimeEntry entry = timeEntryRepository.create(timeEntryToCreate);
            actionCounter.increment();
            timeEntrySummary.record(timeEntryRepository.list().size());
            return new ResponseEntity(entry, HttpStatus.CREATED);
        } catch (Exception ex) {
            return  new ResponseEntity(ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<TimeEntry> read(@PathVariable long id) {

            TimeEntry entry = timeEntryRepository.find(id);
            if(entry != null) {
                actionCounter.increment();
                return new ResponseEntity(entry, HttpStatus.OK);
            }
            return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @GetMapping
    public ResponseEntity<List<TimeEntry>> list() {
        actionCounter.increment();
        return new ResponseEntity(timeEntryRepository.list(), HttpStatus.OK);
    }

    @PutMapping("{id}")
    public ResponseEntity update(@PathVariable long id, @RequestBody TimeEntry expected) {
        TimeEntry entry = timeEntryRepository.update(id, expected);
        if(entry != null) {
            actionCounter.increment();
            return new ResponseEntity(entry, HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("{id}")
    public ResponseEntity delete(@PathVariable long id) {
        actionCounter.increment();
        TimeEntry entry = timeEntryRepository.find(id);
            timeEntryRepository.delete(id);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}

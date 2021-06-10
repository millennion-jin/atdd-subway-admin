package nextstep.subway.line.application;

import nextstep.subway.domain.Section;
import nextstep.subway.domain.Station;
import nextstep.subway.exception.DuplicateDataException;
import nextstep.subway.exception.NoSuchDataException;
import nextstep.subway.domain.Line;
import nextstep.subway.line.repository.LineRepository;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.LinesSubResponse;
import nextstep.subway.section.dto.SectionRequest;
import nextstep.subway.section.dto.SectionResponse;
import nextstep.subway.section.repository.SectionRepository;
import nextstep.subway.station.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LineService {
    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final SectionRepository sectionRepository;

    public LineService(LineRepository lineRepository, StationRepository stationRepository, SectionRepository sectionRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
        this.sectionRepository = sectionRepository;
    }

    public LineResponse saveLine(LineRequest request, SectionResponse sectionResponse) {
        Station upStation = findStationById(request.getUpStationId());
        Station downStation = findStationById(request.getDownStationId());
        Section section = findSectionById(sectionResponse.getId());

        Line line = Line.createWithSectionAndStation(request.getName(), request.getColor(), section, upStation, downStation);
        Line savedLine = lineRepository.save(line);
        return LineResponse.of(savedLine);
    }

    private Section findSectionById(Long sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NoSuchDataException("구간이 존재하지 않습니다.", "sectionId",
                        String.valueOf(sectionId), null));
    }

    private Station findStationById(Long stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new NoSuchDataException("역이 존재하지 않습니다.", "upStationId",
                        String.valueOf(stationId), null));
    }

    public void changeLine(Long lineId, LineRequest lineRequest) {
        Line line = findLineById(lineId);
        line.change(lineRequest.getName(), lineRequest.getColor());
    }

    public void removeLine(Long lineId) {
        lineRepository.deleteById(lineId);
    }

    @Transactional(readOnly = true)
    public void validateDuplicatedName(LineRequest lineRequest) throws NoSuchFieldException {
        if (lineRepository.existsByName(lineRequest.getName())) {
            throw new DuplicateDataException("이미 존재하는 노선 이름입니다.",
                    lineRequest.getClass().getDeclaredField("name").getName(),
                    lineRequest.getName(), lineRequest.getClass().getName());
        }
    }

    @Transactional(readOnly = true)
    public LinesSubResponse readLine(Long lineId) {
        Line line = findLineById(lineId);
        return LinesSubResponse.of(line);
    }

    @Transactional(readOnly = true)
    public List<LinesSubResponse> readLineAll() {
        List<Line> lines = lineRepository.findAll();
        return lines.stream()
                .map(LinesSubResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    private Line findLineById(Long lineId) {
        return lineRepository.findById(lineId).orElseThrow(() -> new NoSuchDataException("존재하지 않는 노선입니다.",
                "lineId", String.valueOf(lineId), null));
    }

    public SectionResponse registerWithLine(Long lineId, SectionRequest sectionRequest, Long sectionId) {
        Line line = findLineById(lineId);
        Station upStation = findStationById(sectionRequest.getUpStationId());
        Station downStation = findStationById(sectionRequest.getDownStationId());
        Section section = findSectionById(sectionId);
        line.register(upStation, downStation, section);
        return SectionResponse.of(section);
    }
}

package nextstep.subway.section;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.LineAcceptanceTest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.dto.StationResponse;
import nextstep.subway.utils.RestAssuredCRUD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nextstep.subway.utils.RestAssuredCRUD.get;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Section에 대한 인수테스트")
public class SectionAcceptanceTest extends AcceptanceTest {

    private StationResponse 강남역;
    private StationResponse 광교역;
    private LineResponse 신분당선;
    private Map<String, String> createParams;

    @BeforeEach
    public void setUp() {
        super.setUp();

        // given
        강남역 = StationAcceptanceTest.지하철역_등록되어_있음("강남역").as(StationResponse.class);
        광교역 = StationAcceptanceTest.지하철역_등록되어_있음("광교역").as(StationResponse.class);

        createParams = new HashMap<>();
        createParams.put("name", "신분당선");
        createParams.put("color", "bg-red-600");
        createParams.put("upStationId", 강남역.getId() + "");
        createParams.put("downStationId", 광교역.getId() + "");
        createParams.put("distance", 10 + "");
        신분당선 = LineAcceptanceTest.지하철_노선_등록되어_있음(createParams).as(LineResponse.class);
    }

    @DisplayName("역 사이에 새로운 역을 등록할 경우 기존 역 사이 길이보다 크거나 같으면 등록을 할 수 없음")
    @Test
    void exceptionTest_1() {
        // when
        // 지하철_노선에_지하철역_등록_요청
        StationResponse 강남역과_광교역_사이의_역 =
                StationAcceptanceTest.지하철역_등록되어_있음("강남역과_광교역_사이의_역")
                        .as(StationResponse.class);

        createParams = new HashMap<>();
        createParams.put("upStationId", 강남역과_광교역_사이의_역.getId() + "");
        createParams.put("downStationId", 광교역.getId() + "");
        createParams.put("distance", 10 + "");

        ExtractableResponse<Response> response = RestAssuredCRUD
                .postRequest("/lines/"+신분당선.getId()+"/sections", createParams);

        // then
        // 지하철_노선에_지하철역_등록_실패함
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 새로운_역을_상행_종점으로_등록할_경우() {
        // when
        // 지하철_노선에_지하철역_등록_요청
        StationResponse 강남역_보다_상행역 =
                StationAcceptanceTest.지하철역_등록되어_있음("강남역_보다_상행역")
                        .as(StationResponse.class);

        createParams = new HashMap<>();
        createParams.put("upStationId", 강남역_보다_상행역.getId() + "");
        createParams.put("downStationId", 강남역.getId() + "");
        createParams.put("distance", 5 + "");

        ExtractableResponse<Response> response = RestAssuredCRUD
                .postRequest("/lines/"+신분당선.getId()+"/sections", createParams);

        // then
        // 지하철_노선에_지하철역_등록됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        LineResponse 구간등록_후_신분당선 = get("/lines/"+신분당선.getId())
                .body()
                .as(LineResponse.class);
        List<String> stations = 구간등록_후_신분당선.getStations().stream()
                .map(stationResponse -> stationResponse.getName())
                .collect(Collectors.toList());
        assertThat(stations).containsExactly("강남역_보다_상행역", "강남역", "광교역");
    }

    @Test
    void 새로운_역을_하행_종점으로_등록할_경우() {
        // when
        // 지하철_노선에_지하철역_등록_요청
        StationResponse 광교역_보다_하행역 =
                StationAcceptanceTest.지하철역_등록되어_있음("광교역_보다_하행역")
                        .as(StationResponse.class);

        createParams = new HashMap<>();
        createParams.put("upStationId", 광교역.getId() + "");
        createParams.put("downStationId", 광교역_보다_하행역.getId() + "");
        createParams.put("distance", 5 + "");

        ExtractableResponse<Response> response = RestAssuredCRUD
                .postRequest("/lines/"+신분당선.getId()+"/sections", createParams);

        // then
        // 지하철_노선에_지하철역_등록됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        LineResponse 구간등록_후_신분당선 = get("/lines/"+신분당선.getId())
                .body()
                .as(LineResponse.class);
        List<String> stations = 구간등록_후_신분당선.getStations().stream()
                .map(stationResponse -> stationResponse.getName())
                .collect(Collectors.toList());
        assertThat(stations).containsExactly("강남역", "광교역", "광교역_보다_하행역");
    }

    @DisplayName("역_사이_뒤쪽에_새로운_역을_등록할_경우")
    @Test
    void addSection_1() {
        // when
        // 지하철_노선에_지하철역_등록_요청
        StationResponse 강남역과_광교역_사이의_역 =
                StationAcceptanceTest.지하철역_등록되어_있음("강남역과_광교역_사이의_역")
                        .as(StationResponse.class);

        createParams = new HashMap<>();
        createParams.put("upStationId", 강남역과_광교역_사이의_역.getId() + "");
        createParams.put("downStationId", 광교역.getId() + "");
        createParams.put("distance", 5 + "");

        ExtractableResponse<Response> response = RestAssuredCRUD
                .postRequest("/lines/"+신분당선.getId()+"/sections", createParams);

        // then
        // 지하철_노선에_지하철역_등록됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        LineResponse 구간등록_후_신분당선 = get("/lines/"+신분당선.getId())
                .body()
                .as(LineResponse.class);
        List<String> stations = 구간등록_후_신분당선.getStations().stream()
                .map(stationResponse -> stationResponse.getName())
                .collect(Collectors.toList());
        assertThat(stations).containsExactly("강남역", "강남역과_광교역_사이의_역", "광교역");
    }

    @DisplayName("역_사이_앞쪽에_새로운_역을_등록할_경우")
    @Test
    void addSection_2() {
        // when
        // 지하철_노선에_지하철역_등록_요청
        StationResponse 강남역과_광교역_사이의_역 =
                StationAcceptanceTest.지하철역_등록되어_있음("강남역과_광교역_사이의_역")
                        .as(StationResponse.class);

        createParams = new HashMap<>();
        createParams.put("upStationId", 강남역.getId() + "");
        createParams.put("downStationId", 강남역과_광교역_사이의_역.getId() + "");
        createParams.put("distance", 5 + "");

        ExtractableResponse<Response> response = RestAssuredCRUD
                .postRequest("/lines/"+신분당선.getId()+"/sections", createParams);

        // then
        // 지하철_노선에_지하철역_등록됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        LineResponse 구간등록_후_신분당선 = get("/lines/"+신분당선.getId())
                .body()
                .as(LineResponse.class);
        List<String> stations = 구간등록_후_신분당선.getStations().stream()
                .map(stationResponse -> stationResponse.getName())
                .collect(Collectors.toList());
        assertThat(stations).containsExactly("강남역", "강남역과_광교역_사이의_역", "광교역");
    }
    
}

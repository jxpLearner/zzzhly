package com.supconit.zzzhly.common;

import com.supconit.mc.api.ConfigClient;
import com.supconit.mc.entity.AggregationCondition;
import com.supconit.mc.entity.SearchBatch;
import com.supconit.mc.entity.SearchCondition;
import com.supconit.mc.entity.SearchResult;
import com.tangguangdi.base.BaseController;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
/**
 * @auther: jxp
 * @date: 2021/3/18 15:07
 * @description:
 */
@FeignClient(
        name = "mc-config-local",
        configuration = {ConfigClient.MultipartSupportConfig.class},
        fallback = ConfigClientLocal.FallBack.class
)
public interface ConfigClientLocal {
    @PostMapping({"/model/device/aggregation"})
    @ApiOperation("类型实例聚合")
    Object aggregation(@RequestBody AggregationCondition aggregationCondition);

    @PostMapping({"/model/device/{id}"})
    @ApiOperation("类型实例新增")
    Object save(@PathVariable("id") String id, @RequestParam(defaultValue = "false") Boolean useCode, @RequestBody List<Map> points);

    @PostMapping({"/model/device/batch"})
    @ApiOperation("类型批量新增")
    Object batchSaveDevice(@RequestParam(defaultValue = "false") Boolean useCode, @RequestBody List<SearchBatch> searchBatches);

    @PutMapping({"/model/device/batch"})
    @ApiOperation("类型批量更新")
    Object batchUpdateDevice(@RequestParam(defaultValue = "false") Boolean useCode, @RequestBody List<SearchBatch> searchBatches);

    /**
     * @deprecated
     */
    @PostMapping({"/model/device/page/{id}"})
    @Deprecated
    @ApiOperation("类型实例分页")
    Object page(@PathVariable String id, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size, @RequestParam(defaultValue = "false") Boolean useCode, @RequestParam(defaultValue = "") String order, @RequestParam(defaultValue = "true") Boolean asc, @RequestBody List<SearchCondition> conditions);

    @PostMapping({"/model/device/search"})
    @ApiOperation("类型实例分页")
    Object search(@RequestBody SearchResult searchResult);

    @PostMapping({"/model/device/complexSearch/{id}"})
    @ApiOperation("复杂实例查询")
    Object complexSearch(@PathVariable String id, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size, @RequestParam(defaultValue = "false", required = false) Boolean useCode, @RequestBody SearchResult condition);

    @PostMapping({"/model/device/sqlSearch"})
    @ApiOperation("SQL查询")
    Object sqlSearch(@RequestParam String[] typeIds, @RequestParam String sql, @RequestBody Map<String, String> params);

    /**
     * @deprecated
     */
    @PostMapping({"/model/device/list/{id}"})
    @Deprecated
    @ApiOperation("类型实例列表")
    Object list(@PathVariable String id, @RequestParam(defaultValue = "false") Boolean useCode, @RequestParam(defaultValue = "") String order, @RequestParam(defaultValue = "true") Boolean asc, @RequestBody List<SearchCondition> conditions);

    @PostMapping({"/model/device/count/{id}"})
    @ApiOperation("类型实例count")
    Object count(@PathVariable String id, @RequestParam(defaultValue = "false") Boolean useCode, @RequestBody List<SearchCondition> conditions);

    @DeleteMapping({"/model/device/{id}"})
    @ApiOperation(
            value = "类型实例删除",
            notes = "类型实例删除"
    )
    Object removeById(@PathVariable("id") String id, @RequestParam(defaultValue = "false") Boolean useCode, @RequestParam(defaultValue = "") String deviceId);

    @DeleteMapping({"/model/device/removeByIds/{id}"})
    @ApiOperation(
            value = "类型实例批量删除",
            notes = "类型实例批量删除"
    )
    Object removeByIds(@PathVariable("id") String id, @RequestParam String[] ids, @RequestParam(defaultValue = "false", required = false) Boolean useCode);

    @PostMapping({"/model/device/removeByCondition"})
    @ApiOperation("条件删除")
    Object removeByCondition(@RequestBody SearchResult searchResult);

    @DeleteMapping({"/model/device/clearById/{id}"})
    @ApiOperation(
            value = "类型实例删除",
            notes = "类型实例删除"
    )
    Object clearById(@PathVariable("id") String id, @RequestParam(defaultValue = "false", required = false) Boolean useCode);

    @PostMapping(
            value = {"/model/device/file/{id}"},
            consumes = {"multipart/form-data"}
    )
    @ApiOperation("实例文件新增")
    Object files(@PathVariable("id") String id, @RequestParam(defaultValue = "false") Boolean useCode, @RequestPart("file") MultipartFile file);

    @Component
    public static class FallBack extends BaseController implements ConfigClientLocal {
        public FallBack() {
        }

        public Object aggregation(AggregationCondition aggregationCondition) {
            return null;
        }

        public Object save(String id, Boolean useCode, List<Map> points) {
            return null;
        }

        public Object batchSaveDevice(Boolean useCode, List<SearchBatch> searchBatches) {
            return null;
        }

        public Object batchUpdateDevice(Boolean useCode, List<SearchBatch> searchBatches) {
            return null;
        }

        public Object page(String id, Integer page, Integer size, Boolean useCode, String order, Boolean asc, List<SearchCondition> conditions) {
            return null;
        }

        public Object search(SearchResult searchResult) {
            return null;
        }

        public Object complexSearch(String id, Integer page, Integer size, Boolean useCode, SearchResult condition) {
            return null;
        }

        public Object sqlSearch(String[] typeIds, String sql, Map<String, String> params) {
            return null;
        }

        public Object list(String id, Boolean useCode, String order, Boolean asc, List<SearchCondition> conditions) {
            return null;
        }

        public Object count(String id, Boolean useCode, List<SearchCondition> conditions) {
            return null;
        }

        public Object removeById(String id, Boolean useCode, String deviceId) {
            return null;
        }

        public Object removeByIds(String id, String[] ids, Boolean useCode) {
            return null;
        }

        public Object removeByCondition(SearchResult searchResult) {
            return null;
        }

        public Object clearById(String id, Boolean useCode) {
            return null;
        }

        public Object files(String id, Boolean useCode, MultipartFile file) {
            return null;
        }
    }
}

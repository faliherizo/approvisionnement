/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.98875, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "06 - Lancer"], "isController": true}, {"data": [1.0, 500, 1500, "04 - Ajouter signataire"], "isController": true}, {"data": [1.0, 500, 1500, "POST /api/clm/trousses/{id}/lancer"], "isController": false}, {"data": [1.0, 500, 1500, "PUT /api/clm/trousses/{id}/parcours"], "isController": false}, {"data": [1.0, 500, 1500, "02 - Creer trousse"], "isController": true}, {"data": [0.995, 500, 1500, "POST /api/auth/login"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/clm/trousses/{id}"], "isController": false}, {"data": [1.0, 500, 1500, "03 - Deposer document"], "isController": true}, {"data": [0.995, 500, 1500, "01 - Login"], "isController": true}, {"data": [0.915, 500, 1500, "08 - Suivi"], "isController": true}, {"data": [1.0, 500, 1500, "POST /api/clm/trousses/{id}/documents"], "isController": false}, {"data": [1.0, 500, 1500, "07 - Detail trousse"], "isController": true}, {"data": [1.0, 500, 1500, "POST /api/clm/trousses"], "isController": false}, {"data": [0.915, 500, 1500, "GET /api/clm/suivi"], "isController": false}, {"data": [1.0, 500, 1500, "POST /api/clm/trousses/{id}/participants"], "isController": false}, {"data": [1.0, 500, 1500, "05 - Definir parcours"], "isController": true}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 800, 0, 0.0, 86.13250000000004, 11, 832, 40.0, 195.0, 337.3999999999992, 598.96, 13.050783862705753, 43.931850692915056, 6.397018864500237], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["06 - Lancer", 100, 0, 0.0, 57.30999999999999, 29, 197, 53.5, 72.9, 85.69999999999993, 196.17999999999958, 1.6951163697387825, 3.5490009143033916, 0.6868532071601715], "isController": true}, {"data": ["04 - Ajouter signataire", 100, 0, 0.0, 29.370000000000005, 13, 83, 27.0, 37.900000000000006, 43.94999999999999, 82.92999999999996, 1.6917897443705696, 1.0308022044020368, 0.9283696222233501], "isController": true}, {"data": ["POST /api/clm/trousses/{id}/lancer", 100, 0, 0.0, 57.30999999999999, 29, 197, 53.5, 72.9, 85.69999999999993, 196.17999999999958, 1.6951451044209385, 3.549061075018647, 0.6868648503186873], "isController": false}, {"data": ["PUT /api/clm/trousses/{id}/parcours", 100, 0, 0.0, 25.509999999999994, 11, 96, 24.0, 35.900000000000006, 42.0, 95.70999999999985, 1.6938530074360145, 1.1444094381489573, 0.8847073657198028], "isController": false}, {"data": ["02 - Creer trousse", 100, 0, 0.0, 54.129999999999995, 28, 324, 50.0, 70.0, 87.59999999999991, 321.8599999999989, 1.674116485025028, 1.787691555547185, 0.7938156829139672], "isController": true}, {"data": ["POST /api/auth/login", 100, 0, 0.0, 177.72000000000003, 125, 832, 163.0, 203.60000000000002, 236.1499999999998, 827.4899999999977, 1.6524555489457333, 1.4313750702293608, 0.6716328119836077], "isController": false}, {"data": ["GET /api/clm/trousses/{id}", 100, 0, 0.0, 25.709999999999997, 13, 61, 24.0, 33.0, 43.89999999999998, 60.989999999999995, 1.7002465357476835, 3.559741748491031, 0.587647708917793], "isController": false}, {"data": ["03 - Deposer document", 100, 0, 0.0, 25.93, 12, 233, 23.0, 32.900000000000006, 40.799999999999955, 231.20999999999907, 1.6860278869012493, 1.242984543339347, 1.4819757032843823], "isController": true}, {"data": ["01 - Login", 100, 0, 0.0, 177.72000000000003, 125, 832, 163.0, 203.60000000000002, 236.1499999999998, 827.4899999999977, 1.6404737688244366, 1.4209963212375734, 0.6667628736179009], "isController": true}, {"data": ["08 - Suivi", 100, 0, 0.0, 293.3799999999999, 53, 735, 272.5, 571.8000000000001, 607.6499999999999, 734.96, 1.6855731791595732, 31.66561559766211, 0.5728315101050112], "isController": true}, {"data": ["POST /api/clm/trousses/{id}/documents", 100, 0, 0.0, 25.93, 12, 233, 23.0, 32.900000000000006, 40.799999999999955, 231.20999999999907, 1.6860278869012493, 1.242984543339347, 1.4819757032843823], "isController": false}, {"data": ["07 - Detail trousse", 100, 0, 0.0, 25.709999999999997, 13, 61, 24.0, 33.0, 43.89999999999998, 60.989999999999995, 1.7002465357476835, 3.559741748491031, 0.587647708917793], "isController": true}, {"data": ["POST /api/clm/trousses", 100, 0, 0.0, 54.129999999999995, 28, 324, 50.0, 70.0, 87.59999999999991, 321.8599999999989, 1.6741725402219951, 1.7877514136294386, 0.7938422626023338], "isController": false}, {"data": ["GET /api/clm/suivi", 100, 0, 0.0, 293.3799999999999, 53, 735, 272.5, 571.8000000000001, 607.6499999999999, 734.96, 1.6855731791595732, 31.66561559766211, 0.5728315101050112], "isController": false}, {"data": ["POST /api/clm/trousses/{id}/participants", 100, 0, 0.0, 29.370000000000005, 13, 83, 27.0, 37.900000000000006, 43.94999999999999, 82.92999999999996, 1.6917897443705696, 1.0308022044020368, 0.9283696222233501], "isController": false}, {"data": ["05 - Definir parcours", 100, 0, 0.0, 25.509999999999994, 11, 96, 24.0, 35.900000000000006, 42.0, 95.70999999999985, 1.6938530074360145, 1.1444094381489573, 0.8847073657198028], "isController": true}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 800, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});

describe("Ext.Date", function() {
    var dateSave,
        dateValue = 0,
        increment = 3,
        OriginalDate = Date,
        PredictableDate = function() {
            return {
                getTime: function() {
                },
                valueOf: function() {
                    dateValue = dateValue + increment;
                    return dateValue;
                }
            };
        };

    function mockDate() {
        Date = PredictableDate;
    };

    afterEach(function() {
        Date = OriginalDate;
        increment += 16;
    });

    it("should get time elapsed in millisecond between date instantiation", function() {
        mockDate();
        var dateA = new PredictableDate();
        expect(Ext.Date.getElapsed(dateA)).toEqual(3);
    });

    it("should get time elapsed in millisecond between two dates", function() {
        mockDate();
        var dateA = new PredictableDate(),
            dateB = new PredictableDate();

        expect(Ext.Date.getElapsed(dateA)).toEqual(19);
    });

    describe("now", function() {
       it("should return the current timestamp", function() {
          var
              millisBeforeCall = +new Date(),
              millisAtCall = Ext.Date.now(),
              millisAfterCall = +new Date();

          expect(millisAtCall).not.toBeLessThan(millisBeforeCall);
          expect(millisAtCall).not.toBeGreaterThan(millisAfterCall);
       });
    });

    describe("getShortMonthName", function() {
       it("should return 3 letter abbreviation for the corresponding month [0-11]", function() {
           expect(Ext.Date.getShortMonthName(0)).toBe("Jan");
           expect(Ext.Date.getShortMonthName(1)).toBe("Feb");
           expect(Ext.Date.getShortMonthName(2)).toBe("Mar");
           expect(Ext.Date.getShortMonthName(3)).toBe("Apr");
           expect(Ext.Date.getShortMonthName(4)).toBe("May");
           expect(Ext.Date.getShortMonthName(5)).toBe("Jun");
           expect(Ext.Date.getShortMonthName(6)).toBe("Jul");
           expect(Ext.Date.getShortMonthName(7)).toBe("Aug");
           expect(Ext.Date.getShortMonthName(8)).toBe("Sep");
           expect(Ext.Date.getShortMonthName(9)).toBe("Oct");
           expect(Ext.Date.getShortMonthName(10)).toBe("Nov");
           expect(Ext.Date.getShortMonthName(11)).toBe("Dec");
       });
    });

    describe("getShortDayName", function() {
       it("should return 3 letter abbreviation for the corresponding weekday [0-6]", function() {
          expect(Ext.Date.getShortDayName(0)).toBe("Sun");
          expect(Ext.Date.getShortDayName(1)).toBe("Mon");
          expect(Ext.Date.getShortDayName(2)).toBe("Tue");
          expect(Ext.Date.getShortDayName(3)).toBe("Wed");
          expect(Ext.Date.getShortDayName(4)).toBe("Thu");
          expect(Ext.Date.getShortDayName(5)).toBe("Fri");
          expect(Ext.Date.getShortDayName(6)).toBe("Sat");

       });
    });

    describe("getMonthNumber", function() {
        it("should return the month number [0-11] for the corresponding short month name", function() {
           expect(Ext.Date.getMonthNumber("jan")).toBe(0);
           expect(Ext.Date.getMonthNumber("feb")).toBe(1);
           expect(Ext.Date.getMonthNumber("mar")).toBe(2);
           expect(Ext.Date.getMonthNumber("apr")).toBe(3);
           expect(Ext.Date.getMonthNumber("MAY")).toBe(4);
           expect(Ext.Date.getMonthNumber("JUN")).toBe(5);
           expect(Ext.Date.getMonthNumber("JUL")).toBe(6);
           expect(Ext.Date.getMonthNumber("AUG")).toBe(7);
           expect(Ext.Date.getMonthNumber("Sep")).toBe(8);
           expect(Ext.Date.getMonthNumber("Oct")).toBe(9);
           expect(Ext.Date.getMonthNumber("Nov")).toBe(10);
           expect(Ext.Date.getMonthNumber("Dec")).toBe(11);
        });
        it("should return the month number [0-11] for the corresponding full month name", function() {
            expect(Ext.Date.getMonthNumber("january")).toBe(0);
            expect(Ext.Date.getMonthNumber("february")).toBe(1);
            expect(Ext.Date.getMonthNumber("march")).toBe(2);
            expect(Ext.Date.getMonthNumber("april")).toBe(3);
            expect(Ext.Date.getMonthNumber("MAY")).toBe(4);
            expect(Ext.Date.getMonthNumber("JUNE")).toBe(5);
            expect(Ext.Date.getMonthNumber("JULY")).toBe(6);
            expect(Ext.Date.getMonthNumber("AUGUST")).toBe(7);
            expect(Ext.Date.getMonthNumber("September")).toBe(8);
            expect(Ext.Date.getMonthNumber("October")).toBe(9);
            expect(Ext.Date.getMonthNumber("November")).toBe(10);
            expect(Ext.Date.getMonthNumber("December")).toBe(11);
        });
    });

    describe("formatContainsHourInfo", function() {
       it("should return true when format contains hour info", function() {
          expect(Ext.Date.formatContainsHourInfo("d/m/Y H:i:s")).toBeTruthy();
       });
       it("should return false when format doesn't contains hour info", function() {
          expect(Ext.Date.formatContainsHourInfo("d/m/Y")).toBeFalsy();
       });
    });

    describe("formatContainsDateInfo", function() {
        it("should return true when format contains date info", function() {
            expect(Ext.Date.formatContainsDateInfo("d/m/Y H:i:s")).toBeTruthy();
        });
        it("should return false when format doesn't contains date info", function() {
            expect(Ext.Date.formatContainsDateInfo("H:i:s")).toBeFalsy();
        });
    });

    describe("isValid", function() {
        it("should return true for valid dates", function() {
           expect(Ext.Date.isValid(1981, 10, 15, 16, 30, 1, 2)).toBeTruthy();
        });
        it("should return false for invalid dates", function() {
            expect(Ext.Date.isValid(999999, 10, 15, 16, 30, 1, 2)).toBeFalsy();
            expect(Ext.Date.isValid(1981, 13, 15, 16, 30, 1, 2)).toBeFalsy();
            expect(Ext.Date.isValid(1981, 10, 32, 16, 30, 1, 2)).toBeFalsy();
            expect(Ext.Date.isValid(1981, 10, 15, 25, 30, 1, 2)).toBeFalsy();
            expect(Ext.Date.isValid(1981, 10, 15, 16, 60, 1, 2)).toBeFalsy();
            expect(Ext.Date.isValid(1981, 10, 15, 16, 30, 60, 2)).toBeFalsy();
            expect(Ext.Date.isValid(1981, 10, 15, 16, 30, 1, 100000)).toBeFalsy();
        });
    });

    describe("parse", function() {
        it("should parse year-only", function() {
            var date = Ext.Date.parse("2011", "Y"),
                expectedDate = new Date();
            expectedDate.setFullYear(2011);
            expectedDate.setHours(0);
            expectedDate.setMinutes(0);
            expectedDate.setSeconds(0);
            expectedDate.setMilliseconds(0);
            expect(date).toEqual(expectedDate);
        });
        it("should parse year-month-date", function() {
            var date = Ext.Date.parse("2011-01-20", "Y-m-d"),
                expectedDate = new Date();
            expectedDate.setFullYear(2011);
            expectedDate.setMonth(0);
            expectedDate.setDate(20);
            expectedDate.setHours(0);
            expectedDate.setMinutes(0);
            expectedDate.setSeconds(0);
            expectedDate.setMilliseconds(0);
            expect(date).toEqual(expectedDate);
        });
        it("should parse year-month-date hour:minute:second am/pm", function() {
            var date = Ext.Date.parse("2011-01-20 6:28:33 PM", "Y-m-d g:i:s A"),
                expectedDate = new Date();
            expectedDate.setFullYear(2011);
            expectedDate.setMonth(0);
            expectedDate.setDate(20);
            expectedDate.setHours(18);
            expectedDate.setMinutes(28);
            expectedDate.setSeconds(33);
            expectedDate.setMilliseconds(0);
            expect(date).toEqual(expectedDate);
        });
        it("should return null when parsing an invalid date like Feb 31st in stric mode", function() {
           expect(Ext.Date.parse("2011-02-31", "Y-m-d", true)).toBeNull();
        });
    });

    describe("isEqual", function() {
        it("should return true if both dates are exactly the same", function() {
            var date1 = new Date(2011, 0, 20, 18, 37, 15, 0),
                date2 = new Date(2011, 0, 20, 18, 37, 15, 0);
            expect(Ext.Date.isEqual(date1, date2)).toBeTruthy();
        });
        it("should return true if there is at least 1 millisecond difference between both dates", function() {
            var date1 = new Date(2011, 0, 20, 18, 37, 15, 0),
                date2 = new Date(2011, 0, 20, 18, 37, 15, 1);
            expect(Ext.Date.isEqual(date1, date2)).toBeFalsy();
        });
        it("should return false if one one of the dates is null/undefined", function() {
           expect(Ext.Date.isEqual(new Date(), undefined)).toBeFalsy();
           expect(Ext.Date.isEqual(new Date(), null)).toBeFalsy();
           expect(Ext.Date.isEqual(undefined, new Date())).toBeFalsy();
           expect(Ext.Date.isEqual(null, new Date())).toBeFalsy();
        });
        it("should return true if both dates are null/undefined", function() {
           expect(Ext.Date.isEqual(null, null)).toBeTruthy();
           expect(Ext.Date.isEqual(null, undefined)).toBeTruthy();
           expect(Ext.Date.isEqual(undefined, null)).toBeTruthy();
           expect(Ext.Date.isEqual(undefined, undefined)).toBeTruthy();
        });
    });

    describe("getDayOfYear", function() {
       it("should return the day of year between 0 and 364 for non-leap years", function() {
           expect(Ext.Date.getDayOfYear(new Date(2001, 0, 1))).toBe(0);
           expect(Ext.Date.getDayOfYear(new Date(2001, 11, 31))).toBe(364);
       });
       it("should return the day of year between 0 and 365 for leap years", function() {
           expect(Ext.Date.getDayOfYear(new Date(2000, 0, 1))).toBe(0);
           expect(Ext.Date.getDayOfYear(new Date(2000, 11, 31))).toBe(365);
       });
    });

    describe("getFirstDayOfMonth", function() {
       it("should return the number [0-6] of the first day of month of the given date", function() {
           expect(Ext.Date.getFirstDayOfMonth(new Date(2007, 0, 1))).toBe(1);
           expect(Ext.Date.getFirstDayOfMonth(new Date(2000, 0, 2))).toBe(6);
           expect(Ext.Date.getFirstDayOfMonth(new Date(2011, 0, 3))).toBe(6);
           expect(Ext.Date.getFirstDayOfMonth(new Date(2011, 6, 4))).toBe(5);
           expect(Ext.Date.getFirstDayOfMonth(new Date(2011, 11, 5))).toBe(4);
       });
    });

    describe("getLastDayOfMonth", function() {
        it("should return the number [0-6] of the last day of month of the given date", function() {
            expect(Ext.Date.getLastDayOfMonth(new Date(2007, 0, 1))).toBe(3);
            expect(Ext.Date.getLastDayOfMonth(new Date(2000, 0, 2))).toBe(1);
            expect(Ext.Date.getLastDayOfMonth(new Date(2011, 0, 3))).toBe(1);
            expect(Ext.Date.getLastDayOfMonth(new Date(2011, 6, 4))).toBe(0);
            expect(Ext.Date.getLastDayOfMonth(new Date(2011, 11, 5))).toBe(6);
        });
    });

    describe("getFirstDateOfMonth", function() {
        it("should return the date corresponding to the first day of month of the given date", function() {
            expect(Ext.Date.getFirstDateOfMonth(new Date(2007, 0, 1))).toEqual(new Date(2007, 0, 1));
            expect(Ext.Date.getFirstDateOfMonth(new Date(2000, 0, 2))).toEqual(new Date(2000, 0, 1));
            expect(Ext.Date.getFirstDateOfMonth(new Date(2011, 0, 3))).toEqual(new Date(2011, 0, 1));
            expect(Ext.Date.getFirstDateOfMonth(new Date(2011, 6, 4))).toEqual(new Date(2011, 6, 1));
            expect(Ext.Date.getFirstDateOfMonth(new Date(2011, 11, 5))).toEqual(new Date(2011, 11, 1));
        });
    });

    describe("getLastDateOfMonth", function() {
        it("should return the date corresponding to the last day of month of the given date", function() {
            expect(Ext.Date.getLastDateOfMonth(new Date(2007, 1, 1))).toEqual(new Date(2007, 1, 28));
            expect(Ext.Date.getLastDateOfMonth(new Date(2000, 1, 2))).toEqual(new Date(2000, 1, 29));
            expect(Ext.Date.getLastDateOfMonth(new Date(2011, 0, 3))).toEqual(new Date(2011, 0, 31));
            expect(Ext.Date.getLastDateOfMonth(new Date(2011, 5, 4))).toEqual(new Date(2011, 5, 30));
            expect(Ext.Date.getLastDateOfMonth(new Date(2011, 11, 5))).toEqual(new Date(2011, 11, 31));
        });
    });

    describe("getSuffix", function() {
       it("should return st for 1, 21 and 31", function() {
          expect(Ext.Date.getSuffix(new Date(2011, 0, 1))).toBe("st");
          expect(Ext.Date.getSuffix(new Date(2011, 0, 21))).toBe("st");
          expect(Ext.Date.getSuffix(new Date(2011, 0, 31))).toBe("st");
       });
       it("should return nd for 2 and, 22", function() {
           expect(Ext.Date.getSuffix(new Date(2011, 0, 2))).toBe("nd");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 22))).toBe("nd");
       });
       it("should return rd for 3 and, 23", function() {
           expect(Ext.Date.getSuffix(new Date(2011, 0, 3))).toBe("rd");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 23))).toBe("rd");
       });
       it("should return th for days [11-13] and days ending in [4-0]", function() {
           expect(Ext.Date.getSuffix(new Date(2011, 0, 4))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 5))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 6))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 7))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 8))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 9))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 10))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 11))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 12))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 13))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 14))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 15))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 16))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 17))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 18))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 19))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 20))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 24))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 25))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 26))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 27))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 28))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 29))).toBe("th");
           expect(Ext.Date.getSuffix(new Date(2011, 0, 30))).toBe("th");
       });
    });

    describe("clone", function() {
       it("should return a copy of the given date", function() {
          var originalDate = new Date(),
              clonedDate;
          clonedDate = Ext.Date.clone(originalDate);
          expect(clonedDate).not.toBe(originalDate);
          expect(clonedDate).toEqual(originalDate);
       });
    });

    describe("isDST", function() {
       it("should return true for the first and last moments of daylight saving time", function() {
           var dstBegin = new Date(2012, 2, 11, 3),
               dstEnd = new Date(2012, 10, 4, 0, 59, 59);
           expect(Ext.Date.isDST(dstBegin)).toBeTruthy();
           expect(Ext.Date.isDST(dstEnd)).toBeTruthy();
       });
       it("should return false for the first and last moments out of daylight saving time", function() {
          var nonDstBegin = new Date(2012, 10, 4, 2),
              nonDstEnd = new Date(2012, 2, 11, 1, 59, 59);
          expect(Ext.Date.isDST(nonDstBegin)).toBeFalsy();
          expect(Ext.Date.isDST(nonDstEnd)).toBeFalsy();
       });
    });

    describe("clearTime", function() {
       it("should reset hrs/mins/secs/millis to 0", function() {
           var date = new Date(2012, 11, 21, 21, 21, 21, 21);
           Ext.Date.clearTime(date);
           expect(date.getHours()).toBe(0);
           expect(date.getMinutes()).toBe(0);
           expect(date.getSeconds()).toBe(0);
           expect(date.getMilliseconds()).toBe(0);
       });
       it("should return a clone with hrs/mins/secs/millis reseted to 0 when clone option is selected", function() {
           var date = new Date(2012, 11, 21, 21, 21, 21, 21),
               clearedTimeDate;
           clearedTimeDate = Ext.Date.clearTime(date, true);
           expect(date.getHours()).toBe(21);
           expect(date.getMinutes()).toBe(21);
           expect(date.getSeconds()).toBe(21);
           expect(date.getMilliseconds()).toBe(21);
           expect(clearedTimeDate.getHours()).toBe(0);
           expect(clearedTimeDate.getMinutes()).toBe(0);
           expect(clearedTimeDate.getSeconds()).toBe(0);
           expect(clearedTimeDate.getMilliseconds()).toBe(0);
       });
    });

    describe("add", function() {
        var date = new Date(2000, 0, 1, 0, 0, 0, 0);
        it("should add milliseconds", function() {
            expect(Ext.Date.add(date, Ext.Date.MILLI, 1)).toEqual(new Date(2000, 0, 1, 0, 0, 0, 1));
        });
        it("should add seconds", function() {
            expect(Ext.Date.add(date, Ext.Date.SECOND, 1)).toEqual(new Date(2000, 0, 1, 0, 0, 1, 0));
        });
        it("should add minutes", function() {
            expect(Ext.Date.add(date, Ext.Date.MINUTE, 1)).toEqual(new Date(2000, 0, 1, 0, 1, 0, 0));
        });
        it("should add hours", function() {
            expect(Ext.Date.add(date, Ext.Date.HOUR, 1)).toEqual(new Date(2000, 0, 1, 1, 0, 0, 0));
        });
        it("should add days", function() {
            expect(Ext.Date.add(date, Ext.Date.DAY, 1)).toEqual(new Date(2000, 0, 2, 0, 0, 0, 0));
        });
        it("should add months", function() {
            expect(Ext.Date.add(date, Ext.Date.MONTH, 1)).toEqual(new Date(2000, 1, 1, 0, 0, 0, 0));
        });
        it("should add years", function() {
           expect(Ext.Date.add(date, Ext.Date.YEAR, 1)).toEqual(new Date(2001, 0, 1, 0, 0, 0, 0));
        });
        it("should consider last day of month when adding months", function() {
           expect(Ext.Date.add(new Date(2001, 0, 29), Ext.Date.MONTH, 1)).toEqual(new Date(2001, 1, 28));
           expect(Ext.Date.add(new Date(2001, 0, 30), Ext.Date.MONTH, 1)).toEqual(new Date(2001, 1, 28));
           expect(Ext.Date.add(new Date(2001, 0, 31), Ext.Date.MONTH, 1)).toEqual(new Date(2001, 1, 28));
           expect(Ext.Date.add(new Date(2000, 0, 29), Ext.Date.MONTH, 1)).toEqual(new Date(2000, 1, 29));
           expect(Ext.Date.add(new Date(2000, 0, 30), Ext.Date.MONTH, 1)).toEqual(new Date(2000, 1, 29));
           expect(Ext.Date.add(new Date(2000, 0, 31), Ext.Date.MONTH, 1)).toEqual(new Date(2000, 1, 29));
        });
        it("should consider last day of month when adding years", function() {
            expect(Ext.Date.add(new Date(2000, 1, 29), Ext.Date.YEAR, 1)).toEqual(new Date(2001, 1, 28));
        });
    });

    describe("between", function() {
        var startDate = new Date(2000, 0, 1),
            endDate = new Date(2000, 0, 31);
        it("should return true if the date is equal to the start date", function() {
            expect(Ext.Date.between(new Date(2000, 0, 1), startDate, endDate)).toBeTruthy();
        });
        it("should return true if the date is equal to the end date", function() {
            expect(Ext.Date.between(new Date(2000, 0, 31), startDate, endDate)).toBeTruthy();
        });
        it("should return true if date is between start and end dates", function() {
            expect(Ext.Date.between(new Date(2000, 0, 15), startDate, endDate)).toBeTruthy();
        });
        it("should return false if date is before start date", function() {
            expect(Ext.Date.between(new Date(1999, 11, 31, 23, 59, 59), startDate, endDate)).toBeFalsy();
        });
        it("should return false if date is after end date", function() {
            expect(Ext.Date.between(new Date(2000, 0, 31, 0, 0, 1), startDate, endDate)).toBeFalsy();
        });
    });

});

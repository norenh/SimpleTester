#!/bin/bash

NR_TESTS=0
TEST_SUCCESS=0

# Temporary output file, will be removed in the end
OUTPUTFILE="$(mktemp)"

function test_check {
	TEST_NAME="$1"
	EXPECTED_RET="$2"
	EXPECTED_OUTTAIL="$3"
	RET="$4"
	PRETEST="$5"

        if [[ "${RET}" -eq "${EXPECTED_RET}" ]] && [[ $(tail -1  "${OUTPUTFILE}") =~ $EXPECTED_OUTTAIL ]] && [[ ${PRETEST} == "true" ]]; then
                echo "PASS - ${TEST_NAME}"
                TEST_SUCCESS=$(( $TEST_SUCCESS + 1 ))
                #tail -2 "${OUTPUTFILE}"
        else
                echo "FAIL - ${TEST_NAME}"
        fi
        NR_TESTS=$(( $NR_TESTS + 1 ))
	unset PRETEST
}

# Test usage-printout if too few arguments, "Usage:" on first line, empty last line
./run.sh > "${OUTPUTFILE}"
RET=$?
if [[ $(head -1  "${OUTPUTFILE}") =~ ^Usage: ]] then
  PRETEST="true";
fi
test_check "Argument - none" 1 "^$" "$RET" "$PRETEST"

# Test broken configs, check for 2 exit value, FAIL line in the end
for FILE in $(ls test/config_broken/config*); do 
	./run.sh -e ERROR -h "${FILE}" file://$(pwd)/test/demopage/demo_page.html test/demopage/script1.txt &> "${OUTPUTFILE}"; 
	test_check "${FILE}" 2 "^FAIL:" "$?" "true"
done

# Test broken scripts, check for 2 exit value, FAIL line in the end
for FILE in $(ls test/script_broken/script*); do
        ./run.sh -e ERROR -h "test/script_broken/config.txt" file://$(pwd)/test/demopage/demo_page.html "${FILE}" &> "${OUTPUTFILE}";
	test_check "${FILE}" 2 "^FAIL:" "$?" "true"
done

# Test missing browser with safari, check for 1 exit value, ERROR line in end
./run.sh -b safari -e ERROR -h test/demopage/config1.txt file://$(pwd)/test/demopage/demo_page.html test/demopage/script1.txt &> "${OUTPUTFILE}";
test_check "Missing browser (safari)" 1 "^ERROR:" "$?" "true"

# Test dryrun of demopage, check for 0 exit value, SUCCESS line in end
./run.sh -t -e ERROR -h test/demopage/config1.txt file://$(pwd)/test/demopage/demo_page.html test/demopage/script1.txt &> "${OUTPUTFILE}";
test_check "Demopage - dryrun" 0 "^SUCCESS:" "$?" "true"

# The next tests will take longer since they start an actual browser and perform run-checks
echo " ---- The following tests are actual browser tests"
echo " ---- They might take some time (minutes not hours) to execute"

# Test wrong URL, check for 3 exit value, ERROR line in end
./run.sh -e ERROR -h test/demopage/config1.txt "https://test.invalid" test/demopage/script1.txt &> "${OUTPUTFILE}";
test_check "Wrong URL" 3 "^ERROR" "$?" "true"

# Test empty script-file, check for 0 exit value, SUCCESS line in end
./run.sh -e ERROR -h test/empty.txt file://$(pwd)/test/demopage/demo_page.html test/empty.txt &> "${OUTPUTFILE}";
test_check "Empty script" 0 "^SUCCESS:" "$?" "true"

# Make sure we have no "test.png" file before we run first test creating it
rm -f "test.png"
# Test demopage, check for 0 exit value, SUCCESS line in end and test.png exist from screenshot
./run.sh -b chrome -e ERROR -h test/demopage/config1.txt file://$(pwd)/test/demopage/demo_page.html test/demopage/script1.txt &> "${OUTPUTFILE}";
RET=$?
if [[ -e "test.png"  ]]; then
  PRETEST="true";
fi
test_check "Demopage - regular (chrome)" 0 "^SUCCESS:" "$RET" "$PRETEST"
rm -f "test.png"

# Test demopage with firefox, check for 0 exit value, SUCCESS line in end and test.png exist from screenshot
./run.sh -b firefox -e ERROR -h test/demopage/config1.txt file://$(pwd)/test/demopage/demo_page.html test/demopage/script1.txt &> "${OUTPUTFILE}";
RET=$?
if [[ -e "test.png"  ]]; then
  PRETEST="true";
fi
test_check "Demopage - regular (firefox)" 0 "^SUCCESS:" "$RET" "$PRETEST"
rm -f "test.png"

# Test demopage with edge, check for 0 exit value, SUCCESS line in end and test.png exist from screenshot
./run.sh -b edge -e ERROR -h test/demopage/config1.txt file://$(pwd)/test/demopage/demo_page.html test/demopage/script1.txt &> "${OUTPUTFILE}";
RET=$?
if [[ -e "test.png"  ]]; then
  PRETEST="true";
fi
test_check "Demopage - regular (edge)" 0 "^SUCCESS:" "$RET" "$PRETEST"
rm -f "test.png"

# Test demopage with multiple script-files, check for 0 exit value, SUCCESS line in end with "(3/3)" in it
./run.sh -e ERROR -h test/demopage/config1.txt file://$(pwd)/test/demopage/demo_page.html test/demopage/scripts/script2.txt test/demopage/scripts/script3.txt test/demopage/scripts/script4.txt &> "${OUTPUTFILE}";
test_check "Demopage - multiple" 0 "^SUCCESS:.*(3/3)" "$?" "true"

# Test demopage with scripts, check for 0 exit value, SUCCESS line in end with "(3/3)" in it
./run.sh -e ERROR -h -s test/demopage/scripts1.txt test/demopage/config1.txt file://$(pwd)/test/demopage/demo_page.html &> "${OUTPUTFILE}";
test_check "Demopage - script" 0 "^SUCCESS:.*(3/3)" "$?" "true"

# Test inputtest with chrome, check for 0 exit value, SUCCESS line in end
./run.sh -b chrome -h -e ERROR test/inputtest/inputconfig.txt file://$(pwd)/test/inputtest/inputtest.html test/inputtest/script1.txt &> "${OUTPUTFILE}";
test_check "Inputtest - regular (chrome)" 0 "^SUCCESS:" "$?" "true"

# Test inputtest with firefox, check for 0 exit value, SUCCESS line in end
./run.sh -b firefox -h -e ERROR test/inputtest/inputconfig.txt file://$(pwd)/test/inputtest/inputtest.html test/inputtest/script1.txt &> "${OUTPUTFILE}";
test_check "Inputtest - regular (firefox)" 0 "^SUCCESS:" "$?" "true"

# Test quirk mode 1,2,3,4 with chrome, check for 0 exit value, SUCCESS line in end
./run.sh -b chrome -q 1,2,3,4 -h -e ERROR test/inputtest/inputconfig.txt file://$(pwd)/test/inputtest/inputtest.html test/inputtest/script1.txt &> "${OUTPUTFILE}";
test_check "Inputtest - quirk 1,2,3,4 (chrome)" 0 "^SUCCESS:" "$?" "true"

# Test quirk mode 1,2,3,4 with firefox, check for 0 exit value, SUCCESS line in end
./run.sh -b firefox -q 1,2,3,4 -h -e ERROR test/inputtest/inputconfig.txt file://$(pwd)/test/inputtest/inputtest.html test/inputtest/script1.txt &> "${OUTPUTFILE}";
test_check "Inputtest - quirk 1,2,3,4 (firefox)" 0 "^SUCCESS:" "$?" "true"

# Test quirk mode 5 with chrome, check for 0 exit value, SUCCESS line in end
./run.sh -b chrome -q 5 -h -e ERROR test/inputtest/inputconfig.txt file://$(pwd)/test/inputtest/inputtest.html test/inputtest/script1.txt &> "${OUTPUTFILE}";
test_check "Inputtest - quirk 5 (chrome)" 0 "^SUCCESS:" "$?" "true"

# Test quirk mode 5 with firefox, check for 0 exit value, SUCCESS line in end
./run.sh -b firefox -q 5 -h -e ERROR test/inputtest/inputconfig.txt file://$(pwd)/test/inputtest/inputtest.html test/inputtest/script1.txt &> "${OUTPUTFILE}";
test_check "Inputtest - quirk 5 (firefox)" 0 "^SUCCESS:" "$?" "true"

# Remove outputfile
rm "${OUTPUTFILE}"

echo
echo "$TEST_SUCCESS out of $NR_TESTS tests succeeded!"


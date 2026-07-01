#!/bin/bash

NR_TESTS=0
TEST_SUCCESS=0

# Temporary output file, will be removed in the end
OUTPUTFILE="$(mktemp)"

# Test usage-printout if too few arguments
./run.sh > "${OUTPUTFILE}"
if [[ "$?" -eq "1" ]] && [[ $(head -1  "${OUTPUTFILE}") =~ ^Usage: ]]; then
	echo "PASS - Argument - none"
 	TEST_SUCCESS=$(( $TEST_SUCCESS + 1 ))
	#head -2 "${OUTPUTFILE}"
else
	echo "FAIL - Argument - none"
fi
NR_TESTS=$(( $NR_TESTS + 1 ))

# Test broken configs, check for 2 exit value, FAIL line in the end
for FILE in $(ls test/config_broken/config*); do 
	./run.sh -e ERROR -h "${FILE}" file://$(pwd)/test/demopage/demo_page.html test/demopage/script1.txt > "${OUTPUTFILE}"; 
	if [[ "$?" -eq "2" ]] && [[ $(tail -1  "${OUTPUTFILE}") =~ ^FAIL: ]]; then
                echo "PASS - ${FILE}"
                TEST_SUCCESS=$(( $TEST_SUCCESS + 1 ))
		#tail -2 "${OUTPUTFILE}"
	else
                echo "FAIL - ${FILE}"
	fi
	NR_TESTS=$(( $NR_TESTS + 1 ))
done

# Test broken scripts, check for 2 exit value, FAIL line in the end
for FILE in $(ls test/script_broken/script*); do
        ./run.sh -e ERROR -h "test/script_broken/config.txt" file://$(pwd)/test/demopage/demo_page.html "${FILE}" > "${OUTPUTFILE}";
        if [[ "$?" -eq "2" ]] && [[ $(tail -1  "${OUTPUTFILE}") =~ ^FAIL: ]]; then
                echo "PASS - ${FILE}"
                TEST_SUCCESS=$(( $TEST_SUCCESS + 1 ))
		#tail -2 "${OUTPUTFILE}"
        else
                echo "FAIL - ${FILE}"
        fi
        NR_TESTS=$(( $NR_TESTS + 1 ))
done

# Test dryrun of demopage, check for 0 exit value, SUCCESS line in end
./run.sh -t -e ERROR -h test/demopage/config1.txt file://$(pwd)/test/demopage/demo_page.html test/demopage/script1.txt > "${OUTPUTFILE}";
if [[ "$?" -eq "0" ]] && [[ $(tail -1  "${OUTPUTFILE}") =~ ^SUCCESS: ]]; then
        echo "PASS - Demopage - dryrun"
        TEST_SUCCESS=$(( $TEST_SUCCESS + 1 ))
        #tail -2 "${OUTPUTFILE}"
else
        echo "FAIL - Demopage - dryrun"
fi
NR_TESTS=$(( $NR_TESTS + 1 ))

# Test demopage, check for 0 exit value, SUCCESS line in end and test.png exist from screenshot
./run.sh -e ERROR -h test/demopage/config1.txt file://$(pwd)/test/demopage/demo_page.html test/demopage/script1.txt > "${OUTPUTFILE}";
if [[ "$?" -eq "0" ]] && [[ $(tail -1  "${OUTPUTFILE}") =~ ^SUCCESS: ]] && [[ -e "test.png"  ]]; then
        echo "PASS - Demopage - regular"
        TEST_SUCCESS=$(( $TEST_SUCCESS + 1 ))
	#tail -2 "${OUTPUTFILE}"
else
        echo "FAIL - Demopage - regular"
fi
NR_TESTS=$(( $NR_TESTS + 1 ))
rm -f "test.png"

# Test demopage with firefox, check for 0 exit value, SUCCESS line in end and test.png exist from screenshot
./run.sh -b firefox -e ERROR -h test/demopage/config1.txt file://$(pwd)/test/demopage/demo_page.html test/demopage/script1.txt > "${OUTPUTFILE}";
if [[ "$?" -eq "0" ]] && [[ $(tail -1  "${OUTPUTFILE}") =~ ^SUCCESS: ]] && [[ -e "test.png"  ]]; then
	echo "PASS - Demopage - regular (firefox)"
        TEST_SUCCESS=$(( $TEST_SUCCESS + 1 ))
        #tail -2 "${OUTPUTFILE}"
else
	echo "FAIL - Demopage - regular (firefox)"
fi
NR_TESTS=$(( $NR_TESTS + 1 ))
rm -f "test.png"

# Test demopage with multiple script-files, check for 0 exit value, SUCCESS line in end with "(3/3)" in it
./run.sh -e ERROR -h test/demopage/config1.txt file://$(pwd)/test/demopage/demo_page.html test/demopage/scripts/script2.txt test/demopage/scripts/script3.txt test/demopage/scripts/script4.txt > "${OUTPUTFILE}";
if [[ "$?" -eq "0" ]] && [[ $(tail -1  "${OUTPUTFILE}") =~ ^SUCCESS:.*(3/3) ]]; then
        echo "PASS - Demopage - multiple"
        TEST_SUCCESS=$(( $TEST_SUCCESS + 1 ))
        #tail -2 "${OUTPUTFILE}"
else
        echo "FAIL - Demopage - multiple"
fi
NR_TESTS=$(( $NR_TESTS + 1 ))

# Test demopage with scripts, check for 0 exit value, SUCCESS line in end with "(3/3)" in it
./run.sh -e ERROR -h -s test/demopage/scripts1.txt test/demopage/config1.txt file://$(pwd)/test/demopage/demo_page.html > "${OUTPUTFILE}";
if [[ "$?" -eq "0" ]] && [[ $(tail -1  "${OUTPUTFILE}") =~ ^SUCCESS:.*(3/3) ]]; then
        echo "PASS - Demopage - script"
        TEST_SUCCESS=$(( $TEST_SUCCESS + 1 ))
        #tail -2 "${OUTPUTFILE}"
else
        echo "FAIL - Demopage - script"
fi
NR_TESTS=$(( $NR_TESTS + 1 ))

# Remove outputfile
rm "${OUTPUTFILE}"

echo
echo "$TEST_SUCCESS out of $NR_TESTS tests succeeded!"


# Functions for customizing colors
print_red(){
    printf "\e[1;31m$1\e[0m"
}
print_green(){
    printf "\e[1;32m$1\e[0m"
}
print_yellow(){
    printf "\e[1;33m$1\e[0m"
}
print_blue(){
    printf "\e[1;34m$1\e[0m"
}

#Start
print_blue "\n\n Starting"

print_blue "\n\n cd into working directory... \n"
cd ..

print_blue "\n\n\nrun unit tests...\n"
./gradlew test --stacktrace
print_green "\n\n\n unit tests COMPLETE.\n"

print_blue "\n\n\n run androidTests...\n"
./gradlew connectedAndroidTest --stacktrace
print_green "\n\n\n androidTests COMPLETE.\n"

print_yellow "\n\n\n All tests completed.\n"

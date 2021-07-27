import { writable } from "svelte/store";
import { LinterResult, NetworkErrorResult } from "./types";

function createLinter() {
    const { subscribe, update } = writable(new LinterState());

    return {
        subscribe,
        setFile: (file: File) => {
            update(state => {
                state.file = file;
                return state;
            });

            const formData = new FormData();
            formData.append('file', file);

            fetch('http://localhost:8080/lint/', {
                method: 'POST',
                body: formData
            }).then(response => {
                return response.json();
            })
                .then(result => update(state => {
                    state.result = result;
                    console.log(result);
                    return state;
                }))
                .catch(error => {
                    console.error('Error: ', error);
                    update(state => {
                        state.result = new NetworkErrorResult();
                        return state;
                    });
                });
        }
    }
}

class LinterState {
    file: File;
    result: LinterResult;
};

export const linter = createLinter();
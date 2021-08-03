import { writable } from "svelte/store";
import { LinterResult, NetworkErrorResult } from "./types";

function createLinter() {
    const { subscribe, update, set } = writable(new LinterState());

    return {
        subscribe,
        setFile: (file: File) => {
            update(state => {
                state.file = file;
                state.loading = true;
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
                    state.loading = false;
                    console.log(result);
                    return state;
                }))
                .catch(error => {
                    console.error('Error: ', error);
                    update(state => {
                        state.result = new NetworkErrorResult();
                        state.loading = false;
                        return state;
                    });
                });
        },
        clear: () => {
            set(new LinterState());
        }
    }
}

class LinterState {
    file: File;
    result: LinterResult;
    loading: boolean;
};

export const linter = createLinter();